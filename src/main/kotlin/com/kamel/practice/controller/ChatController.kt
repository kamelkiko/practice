package com.kamel.practice.controller

import com.kamel.practice.controller.dto.MessageRequestDto
import com.kamel.practice.controller.dto.MessageResponseDto
import com.kamel.practice.model.MessageType
import com.kamel.practice.service.ChatService
import com.kamel.practice.util.ActiveUsersResponse
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

/**
 * WebSocket controller for handling real-time chat messages
 * Uses Spring's @MessageMapping for STOMP message handling
 */
@Controller
class ChatController(
    private val chatService: ChatService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    /**
     * Handle incoming chat messages
     * Maps to /app/chat.sendMessage destination
     */
    @MessageMapping("/chat.sendMessage")
    fun sendMessage(@Payload messageRequest: MessageRequestDto) {
        try {
            logger.debug("Received message from ${messageRequest.sender} in room ${messageRequest.roomId}")

            // Save message to database
            val savedMessage = chatService.saveMessage(messageRequest)
            val messageResponse = MessageResponseDto.from(savedMessage)

            // Update user's last seen timestamp
            chatService.updateUserLastSeen(messageRequest.sender, messageRequest.roomId)

            // Broadcast message to all subscribers of the room
            messagingTemplate.convertAndSend(
                "/topic/room/${messageRequest.roomId}",
                messageResponse
            )

            logger.debug("Message broadcasted to room ${messageRequest.roomId}")

        } catch (e: Exception) {
            logger.error("Error processing message: ${e.message}", e)
            // Send error message back to sender
            messagingTemplate.convertAndSendToUser(
                messageRequest.sender,
                "/queue/errors",
                mapOf("error" to "Failed to send message: ${e.message}")
            )
        }
    }

    /**
     * Handle user joining a room
     * Maps to /app/chat.addUser destination
     */
    @MessageMapping("/chat.addUser")
    fun addUser(
        @Payload messageRequest: MessageRequestDto,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        try {
            logger.info("User ${messageRequest.sender} joining room ${messageRequest.roomId}")

            // Store username in WebSocket session
            headerAccessor.sessionAttributes?.put("username", messageRequest.sender)
            headerAccessor.sessionAttributes?.put("roomId", messageRequest.roomId)

            // Add user to room in database
            chatService.addUserToRoom(messageRequest.sender, messageRequest.roomId)

            // Create join message
            val joinMessage = messageRequest.copy(
                content = "${messageRequest.sender} joined the chat",
                type = MessageType.JOIN
            )

            val savedMessage = chatService.saveMessage(joinMessage)
            val messageResponse = MessageResponseDto.from(savedMessage)

            // Broadcast join message to room
            messagingTemplate.convertAndSend(
                "/topic/room/${messageRequest.roomId}",
                messageResponse
            )

            // Send current active users to the new user
            val activeUsers = ActiveUsersResponse.from(chatService.getActiveUsersInRoom(messageRequest.roomId))
            messagingTemplate.convertAndSendToUser(
                messageRequest.sender,
                "/queue/users",
                activeUsers
            )

            // Broadcast updated user list to all room members
            messagingTemplate.convertAndSend(
                "/topic/room/${messageRequest.roomId}/users",
                activeUsers
            )

            logger.info("User ${messageRequest.sender} successfully joined room ${messageRequest.roomId}")

        } catch (e: Exception) {
            logger.error("Error adding user to room: ${e.message}", e)
            messagingTemplate.convertAndSendToUser(
                messageRequest.sender,
                "/queue/errors",
                mapOf("error" to "Failed to join room: ${e.message}")
            )
        }
    }

    /**
     * Handle typing indicators
     * Maps to /app/chat.typing destination
     */
    @MessageMapping("/chat.typing")
    fun handleTyping(@Payload typingData: Map<String, String>) {
        val username = typingData["username"]
        val roomId = typingData["roomId"]
        val isTyping = typingData["isTyping"]?.toBoolean() ?: false
        val userTyping = UserTypingData.from(username ?: "Unknown", isTyping)
        if (username != null && roomId != null) {
            // Broadcast typing indicator to room (except sender)
            messagingTemplate.convertAndSend(
                "/topic/room/$roomId/typing",
                userTyping
            )
        }
    }

    /**
     * Handle private messages between users
     * Maps to /app/chat.private destination
     */
    @MessageMapping("/chat.private")
    fun sendPrivateMessage(@Payload privateMessage: Map<String, String>) {
        val sender = privateMessage["sender"]
        val recipient = privateMessage["recipient"]
        val content = privateMessage["content"]

        if (sender != null && recipient != null && content != null) {
            val message = mapOf(
                "sender" to sender,
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )

            // Send message to specific user
            messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/private",
                message
            )

            logger.debug("Private message sent from $sender to $recipient")
        }
    }
}

data class UserTypingData(
    val username: String,
    val isTyping: Boolean
) {
    companion object {
        fun from(username: String, isTyping: Boolean) = UserTypingData(username, isTyping)
    }
}