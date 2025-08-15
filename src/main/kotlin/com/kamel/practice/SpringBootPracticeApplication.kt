package com.kamel.practice

import com.kamel.practice.controller.Config
import com.kamel.practice.controller.GameWebSocketHandler
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@SpringBootApplication
@EnableConfigurationProperties(
    Config::class
)
class SpringBootPracticeApplication

fun main(args: Array<String>) {
    runApplication<SpringBootPracticeApplication>(*args)
}

@Configuration
@EnableWebSocket
class WebSocketConfig(private val gameHandler: GameWebSocketHandler) : WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(gameHandler, "/ws/game").setAllowedOrigins("*")
    }
}

@Component
class MyWebSocketHandler : WebSocketHandler {
    // Store sessions per room
    private val rooms: MutableMap<String, MutableSet<WebSocketSession>> = mutableMapOf()

    private fun getRoomFromQuery(session: WebSocketSession): String {
        val uri = session.uri
        val query = uri?.query
        return query?.substringAfter("room=", "default") ?: "default"
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val room = getRoomFromQuery(session) // e.g., ?room=123
        rooms.computeIfAbsent(room) { mutableSetOf() }.add(session)
        println("WebSocket connection established: ${session.id}")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val room = getRoomFromQuery(session)
        val payload = message.payload.toString()

        println("Received from room $room: $payload")

        // Broadcast to all in the same room
        rooms[room]?.forEach { client ->
            if (client.isOpen && client.id != session.id) {
                client.sendMessage(TextMessage("From ${session.id}: $payload"))
            }
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        println("WebSocket transport error: ${exception.message} for session: ${session.id}")
        // Optionally close the session on error
        try {
            rooms.values.forEach { it.remove(session) }
            session.close(CloseStatus.SERVER_ERROR)
        } catch (e: Exception) {
            println("Error closing session: ${e.message}")
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        rooms.values.forEach { it.remove(session) }
        println("WebSocket connection closed: ${session.id} with status: $closeStatus")
    }

    override fun supportsPartialMessages(): Boolean = false
}