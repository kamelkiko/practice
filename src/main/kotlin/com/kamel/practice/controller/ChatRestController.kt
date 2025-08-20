package com.kamel.practice.controller

import com.kamel.practice.controller.dto.MessageResponseDto
import com.kamel.practice.controller.dto.RoomResponseDto
import com.kamel.practice.service.ChatService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

/**
 * REST API controller for chat operations
 * Provides HTTP endpoints for room management and message history
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = ["*"]) // Configure properly for production
class ChatRestController(
    private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(ChatRestController::class.java)

    /**
     * Get all active chat rooms
     */
    @GetMapping("/rooms")
    fun getAllRooms(): ResponseEntity<List<RoomResponseDto>> {
        logger.debug("Fetching all active rooms")
        val rooms = chatService.getActiveRooms()
        return ResponseEntity.ok(rooms)
    }

    /**
     * Create a new chat room
     */
    @PostMapping("/rooms")
    fun createRoom(@RequestBody request: CreateRoomRequest): ResponseEntity<Any> {
        return try {
            logger.info("Creating new room: ${request.name}")
            val room = chatService.createRoom(request.name, request.description, request.createdBy)
            ResponseEntity.status(HttpStatus.CREATED).body(
                mapOf(
                    "id" to room.id,
                    "name" to room.name,
                    "description" to room.description,
                    "createdBy" to room.createdBy,
                    "createdAt" to room.createdAt
                )
            )
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to create room: ${e.message}")
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Error creating room: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create room"))
        }
    }

    /**
     * Get room details by ID
     */
    @GetMapping("/rooms/{roomId}")
    fun getRoomDetails(@PathVariable roomId: String): ResponseEntity<Any> {
        logger.debug("Fetching details for room: $roomId")
        val room = chatService.getRoomById(roomId)

        return if (room != null) {
            val activeUsers = chatService.getActiveUsersInRoom(roomId)
            val messageCount = chatService.getMessageCount(roomId)

            ResponseEntity.ok(
                mapOf(
                    "id" to room.id,
                    "name" to room.name,
                    "description" to room.description,
                    "createdBy" to room.createdBy,
                    "createdAt" to room.createdAt,
                    "activeUsers" to activeUsers,
                    "messageCount" to messageCount
                )
            )
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get recent messages for a room
     */
    @GetMapping("/rooms/{roomId}/messages")
    fun getRecentMessages(
        @PathVariable roomId: String,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<List<MessageResponseDto>> {
        logger.debug("Fetching recent messages for room: $roomId, limit: $limit")
        val messages = chatService.getRecentMessages(roomId, limit)
        return ResponseEntity.ok(messages)
    }

    /**
     * Get messages after a specific timestamp
     */
    @GetMapping("/rooms/{roomId}/messages/after")
    fun getMessagesAfter(
        @PathVariable roomId: String,
        @RequestParam timestamp: String
    ): ResponseEntity<List<MessageResponseDto>> {
        return try {
            val dateTime = LocalDateTime.parse(timestamp)
            val messages = chatService.getMessagesAfter(roomId, dateTime)
            ResponseEntity.ok(messages)
        } catch (e: Exception) {
            logger.warn("Invalid timestamp format: $timestamp")
            ResponseEntity.badRequest().build()
        }
    }

    /**
     * Get active users in a room
     */
    @GetMapping("/rooms/{roomId}/users")
    fun getActiveUsers(@PathVariable roomId: String): ResponseEntity<Map<String, Any>> {
        logger.debug("Fetching active users for room: $roomId")
        val users = chatService.getActiveUsersInRoom(roomId)
        return ResponseEntity.ok(
            mapOf(
                "roomId" to roomId,
                "users" to users,
                "count" to users.size
            )
        )
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "OK",
                "timestamp" to LocalDateTime.now().toString()
            )
        )
    }
}

/**
 * Request DTO for creating a new room
 */
data class CreateRoomRequest(
    val name: String,
    val description: String?,
    val createdBy: String
)