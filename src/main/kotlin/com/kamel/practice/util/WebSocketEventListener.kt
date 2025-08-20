package com.kamel.practice.util

import com.kamel.practice.controller.dto.MessageRequestDto
import com.kamel.practice.controller.dto.MessageResponseDto
import com.kamel.practice.model.MessageType
import com.kamel.practice.service.ChatService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

/**
 * WebSocket event listener to handle connection and disconnection events
 * Manages user presence and cleanup operations
 */
@Component
class WebSocketEventListener(
    private val messagingTemplate: SimpMessageSendingOperations,
    private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    /**
     * Handle WebSocket connection established
     */
    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectedEvent) {
        val sessionId = StompHeaderAccessor.wrap(event.message).sessionId
        logger.info("WebSocket connection established for session: $sessionId")
    }

    /**
     * Handle WebSocket disconnection
     * Clean up user presence and send leave message
     */
    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = headerAccessor.sessionId
        val username = headerAccessor.sessionAttributes?.get("username") as? String
        val roomId = headerAccessor.sessionAttributes?.get("roomId") as? String

        logger.info("WebSocket disconnection for session: $sessionId, user: $username, room: $roomId")

        if (username != null && roomId != null) {
            try {
                // Remove user from room
                chatService.removeUserFromRoom(username, roomId)

                // Create and save leave message
                val leaveMessage = MessageRequestDto(
                    content = "$username left the chat",
                    sender = username,
                    roomId = roomId,
                    type = MessageType.LEAVE
                )

                val savedMessage = chatService.saveMessage(leaveMessage)

                // Broadcast leave message
                messagingTemplate.convertAndSend(
                    "/topic/room/$roomId",
                    MessageResponseDto.from(savedMessage)
                )

                // Update and broadcast active users list
                val activeUsers = ActiveUsersResponse.from(chatService.getActiveUsersInRoom(roomId))
                messagingTemplate.convertAndSend(
                    "/topic/room/$roomId/users",
                    activeUsers
                )

                logger.info("User $username disconnected and removed from room $roomId")

            } catch (e: Exception) {
                logger.error("Error handling user disconnection: ${e.message}", e)
            }
        }
    }
}

data class ActiveUsersResponse(
    val users: List<String>
) {
    companion object {
        fun from(users: List<String>): ActiveUsersResponse {
            return ActiveUsersResponse(users)
        }
    }
}