package com.kamel.practice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kamel.practice.service.Card
import com.kamel.practice.service.LiarGameService
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class GameWebSocketHandler(
    private val game: LiarGameService,
    private val mapper: ObjectMapper
) : TextWebSocketHandler() {

    // sessions per room for broadcasting
    private val roomSessions = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val sessionToRoom = ConcurrentHashMap<String, String>()
    private val joinTimeoutScheduler = Executors.newScheduledThreadPool(1)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        // OPTIONAL: auto-join if query already has room & name:
        val uri = session.uri
        val qp = uri?.query.orEmpty()
            .split("&").mapNotNull {
                val i = it.indexOf("="); if (i > 0) it.substring(0, i) to it.substring(i + 1) else null
            }.toMap()

        val roomFromQuery = qp["room"]
        val nameFromQuery = qp["playerName"]

        if (roomFromQuery != null && nameFromQuery != null) {
            // Auto-JOIN without waiting for a message
            joinRoom(roomFromQuery, nameFromQuery, session)
        } else {
            // Enforce that client must send JOIN within 10 seconds
            joinTimeoutScheduler.schedule({
                if (!sessionToRoom.containsKey(session.id) && session.isOpen) {
                    errorTo(session, "JOIN_TIMEOUT")
                    session.close(CloseStatus.POLICY_VIOLATION)
                }
            }, 10, TimeUnit.SECONDS)
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val node = mapper.readTree(message.payload)
        println("KIKO" + node.get("type").asText())
        when (node.get("type").asText()) {
            "JOIN" -> {
                val room = node.get("room").asText()
                val name = node.get("playerName").asText()
                joinRoom(room, name, session)
            }

            "START" -> {
                val room = sessionToRoom[session.id] ?: return errorTo(session, "JOIN_FIRST")
                try {
                    game.start(room, session.id)
                    broadcastAll(room, mapOf("event" to "GAME_STARTED"))
                    sendPrivateHands(room)
                    broadcastTurn(room) // ✅ tell everyone whose turn it is
                } catch (e: Exception) {
                    errorTo(session, e.message ?: "START_FAILED")
                }
            }

            "PLAY" -> {
                val room = sessionToRoom[session.id] ?: return errorTo(session, "JOIN_FIRST")
                val cards = node.get("cards").map { parseCard(it.asText()) }
                val claim = node.get("claim").asText()
                try {
                    game.play(room, session.id, cards, claim)
                    broadcast(
                        room,
                        session,
                        mapOf("event" to "PLAY_ACCEPTED", "by" to session.id, "count" to cards.size)
                    )
                    broadcastAll(room, mapOf("event" to "TURN")) // include next player id in real impl
                    sendPrivateHand(room, session)
                    broadcastTurn(room)
                } catch (e: IllegalStateException) {
                    errorTo(session, e.message ?: "ILLEGAL_MOVE")
                } catch (e: IllegalArgumentException) {
                    errorTo(session, e.message ?: "BAD_REQUEST")
                }
            }

            "PASS" -> {
                val room = sessionToRoom[session.id] ?: return errorTo(session, "JOIN_FIRST")
                try {
                    game.pass(room, session.id)
                    broadcastAll(room, mapOf("event" to "TURN"))
                    broadcastTurn(room)
                } catch (e: Exception) {
                    errorTo(session, e.message ?: "ILLEGAL_MOVE")
                }
            }

            "CALL_LIAR" -> {
                val room = sessionToRoom[session.id] ?: return errorTo(session, "JOIN_FIRST")
                val truth = game.callLiar(room, session.id)
                broadcastAll(room, mapOf("event" to "LIAR_RESULT", "truth" to truth))
                sendPrivateHands(room)
                broadcastTurn(room)
            }

            "PING" -> session.sendMessage(TextMessage("""{"event":"PONG"}"""))
            else -> errorTo(session, "UNKNOWN_TYPE")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val room = sessionToRoom.remove(session.id) ?: return
        roomSessions[room]?.remove(session)
        game.leave(room, session.id)
        broadcastAll(room, mapOf("event" to "PLAYER_LEFT", "id" to session.id))
        if (roomSessions[room]?.isEmpty() == true) roomSessions.remove(room)
    }

    // --- helpers ---
    private fun broadcastTurn(room: String) {
        val players = game.getPlayers(room)
        val currentPlayerId = players[game.getCurrentTurnIndex(room)].id
        broadcastAll(room, mapOf("event" to "TURN", "playerId" to currentPlayerId))
    }

    private fun joinRoom(room: String, name: String, session: WebSocketSession) {
        try {
            game.join(room, name, session.id)
            roomSessions.computeIfAbsent(room) { mutableSetOf() }.add(session)
            sessionToRoom[session.id] = room

            // Tell others someone joined (not the full hand!)
            broadcast(room, session, mapOf("event" to "PLAYER_JOINED", "playerId" to session.id, "name" to name))

            // Tell the joiner who else is in and counts (no leakage)
            val players = game.getPlayers(room)
            val others = players.filter { it.sessionId != session.id }
                .map { mapOf("playerId" to it.id, "name" to it.name, "handSize" to it.hand.size) }

            val youHand = game.getHandFor(room, session.id)
            session.sendMessage(
                TextMessage(
                    mapper.writeValueAsString(
                        mapOf(
                            "event" to "JOINED",
                            "room" to room,
                            "youId" to players.find { it.sessionId == session.id }?.id,
                            "youHave" to youHand,          // PRIVATE: only to joiner
                            "others" to others             // only sizes & names
                        )
                    )
                )
            )
        } catch (e: Exception) {
            errorTo(session, e.message ?: "JOIN_FAILED")
        }
    }

    /** Send each player's hand privately after START (or any state reset) */
    private fun sendPrivateHands(room: String) {
        val sessions = roomSessions[room].orEmpty()
        val players = game.getPlayers(room)
        val sizesById = players.associate { it.id to it.hand.size }

        sessions.forEach { s ->
            if (s.isOpen) {
                val youHand = game.getHandFor(room, s.id)
                val youId = players.find { it.sessionId == s.id }?.id
                val others = players.filter { it.sessionId != s.id }
                    .map { mapOf("playerId" to it.id, "name" to it.name, "handSize" to sizesById[it.id]) }

                val payload = mapOf(
                    "event" to "HAND_UPDATE",
                    "youId" to youId,
                    "youHave" to youHand,   // full cards ONLY to this session
                    "others" to others      // no cards; just sizes
                )
                s.sendMessage(TextMessage(mapper.writeValueAsString(payload)))
            }
        }
    }

    /** Send only to one player (e.g., after they play) */
    private fun sendPrivateHand(room: String, session: WebSocketSession) {
        if (!session.isOpen) return
        val players = game.getPlayers(room)
        val youHand = game.getHandFor(room, session.id)
        val youId = players.find { it.sessionId == session.id }?.id
        val payload = mapOf(
            "event" to "HAND_UPDATE",
            "youId" to youId,
            "youHave" to youHand
        )
        session.sendMessage(TextMessage(mapper.writeValueAsString(payload)))
    }

//    private fun roomOf(session: WebSocketSession): String? =
//        roomSessions.entries.firstOrNull { it.value.contains(session) }?.key

    private fun broadcast(room: String, from: WebSocketSession, payload: Any) {
        val text = TextMessage(mapper.writeValueAsString(payload))
        roomSessions[room]?.forEach { s -> if (s.isOpen && s.id != from.id) s.sendMessage(text) }
    }

    private fun broadcastAll(room: String, payload: Any) {
        val text = TextMessage(mapper.writeValueAsString(payload))
        roomSessions[room]?.forEach { s -> if (s.isOpen) s.sendMessage(text) }
    }

    private fun errorTo(session: WebSocketSession, code: String) {
        session.sendMessage(TextMessage("""{"event":"ERROR","code":"$code"}"""))
    }

    private fun parseCard(raw: String): Card {
        // e.g., "7♣" or "10♦"
        val suit = raw.last().toString()
        val rank = raw.removeSuffix(suit)
        return Card(rank, suit)
    }
}