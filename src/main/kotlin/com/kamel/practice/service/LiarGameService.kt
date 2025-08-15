package com.kamel.practice.service

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class LiarGameService {

    private val rooms = ConcurrentHashMap<String, GameRoom>()
    private val locks = ConcurrentHashMap<String, ReentrantLock>()

    fun join(roomId: String, playerName: String, sessionId: String): Player {
        val room = rooms.computeIfAbsent(roomId) { GameRoom(roomId) }
        val lock = locks.computeIfAbsent(roomId) { ReentrantLock() }
        check(room.players.size < 4) { "Room is full (max 4 players)" }
        lock.withLock {
            check(room.phase == Phase.LOBBY) { "Game already started" }
            val player = Player(
                id = "p${room.players.size + 1}",
                name = playerName,
                sessionId = sessionId,
                hand = mutableListOf()
            )
            room.players += player
            if (room.players.size == 1 || room.hostId.isNullOrEmpty()) {
                room.hostId = player.id
            }
            return player
        }
    }

    fun start(roomId: String, sessionId: String) {
        withRoom(roomId) { room ->
            check(room.phase == Phase.LOBBY) { "Already started" }
            val starter = room.players.find { it.sessionId == sessionId } ?: error("Player not found")
            check(starter.id == room.hostId) { "Only host can start" }
            deal(room)
            room.phase = Phase.PLAYING
            room.turnIndex = 0
            room.nextRequiredRank = "A"
        }
    }

    fun play(roomId: String, sessionId: String, facedown: List<Card>, claimedRank: String) {
        withRoom(roomId) { room ->
            val player = room.players.find { it.sessionId == sessionId } ?: error("Player not found")
            checkTurn(room, player)
            require(facedown.isNotEmpty()) { "Must play at least 1 card" }
            require(facedown.size <= 3) { "Max 3 cards per play" }
            if (room.pile.isEmpty()) {
                // First play of round → set claim
                room.currentClaim = claimedRank
            } else {
                // Not first play → must match claim
                require(claimedRank == room.currentClaim) { "Claim must match current round's claim" }
            }
            // validate player actually holds those cards
            require(facedown.all { c -> player.hand.contains(c) }) { "You don't hold all played cards" }
            // remove from hand and push to pile (face-down)
            facedown.forEach { player.hand.remove(it) }
            room.pile.addAll(facedown)
            room.lastPlay = Play(player.id, facedown, claimedRank)
            advanceTurn(room)
        }
    }

    fun pass(roomId: String, sessionId: String) {
        withRoom(roomId) { room ->
            val player = room.players.find { it.sessionId == sessionId } ?: error("Player not found")
            checkTurn(room, player)
            advanceTurn(room)
        }
    }

    fun callLiar(roomId: String, sessionId: String): Boolean {
        return withRoom(roomId) { room ->
            val challenger = room.players.find { it.sessionId == sessionId } ?: error("Player not found")
            check(room.lastPlay != null) { "No play to challenge" }
            val lp = room.lastPlay!!
            val wasTruth = lp.facedown.all { it.rank == lp.claimedRank }

            val loser = if (wasTruth) challenger else room.players.first { it.id == lp.playerId }
            loser.hand.addAll(room.pile)
            room.pile.clear()
            room.lastPlay = null
            room.currentClaim = null
            // after liar resolution, turn goes to loser
            room.turnIndex = room.players.indexOf(loser)
            wasTruth
        }
    }

    fun leave(roomId: String, sessionId: String) {
        withRoom(roomId) { room ->
            val idx = room.players.indexOfFirst { it.sessionId == sessionId }
            if (idx >= 0) {
                val leaving = room.players.removeAt(idx)
                // give hand to pile (or define your rule)
                room.pile.addAll(leaving.hand)
                if (room.players.isEmpty()) {
                    rooms.remove(roomId); locks.remove(roomId)
                } else if (room.turnIndex >= room.players.size) {
                    room.turnIndex = room.turnIndex % room.players.size
                }
            }
        }
    }

    fun getPlayers(roomId: String): List<Player> =
        withRoom(roomId) { it.players.map { p -> p.copy(hand = p.hand.toMutableList()) } }

    fun getHandFor(roomId: String, sessionId: String): List<Card> =
        withRoom(roomId) {
            val p = it.players.find { pl -> pl.sessionId == sessionId } ?: error("Player not found")
            p.hand.toList()
        }

//    fun roomExists(roomId: String): Boolean = try {
//        withRoom(roomId) { true }
//    } catch (_: Exception) {
//        false
//    }

    private fun deal(room: GameRoom) {
        // Evenly distribute cards; or your variant
        while (room.deck.isNotEmpty()) {
            room.players.forEach { p ->
                room.deck.removeFirstOrNull()?.let { p.hand += it }
            }
        }
    }

    private fun advanceTurn(room: GameRoom) {
        room.turnIndex = (room.turnIndex + 1) % room.players.size
        room.nextRequiredRank = nextRank(room.nextRequiredRank)
    }

    private fun nextRank(rank: String): String {
        val order = listOf("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A")
        val i = (order.indexOf(rank) + 1) % order.size
        return order[i]
    }

    private fun checkTurn(room: GameRoom, player: Player) {
        check(room.phase == Phase.PLAYING) { "Game not started" }
        check(room.currentPlayerId() == player.id) { "Not your turn" }
    }

    private fun <T> withRoom(roomId: String, block: (GameRoom) -> T): T {
        val lock = locks.computeIfAbsent(roomId) { ReentrantLock() }
        val room = rooms[roomId] ?: error("Room not found")
        return lock.withLock { block(room) }
    }

    fun getCurrentTurnIndex(roomId: String): Int =
        withRoom(roomId) { it.turnIndex }
}