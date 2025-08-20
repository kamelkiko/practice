package com.kamel.practice.service

import com.kamel.practice.controller.MessageRequestDto
import com.kamel.practice.controller.MessageResponseDto
import com.kamel.practice.controller.RoomResponseDto
import com.kamel.practice.model.ActiveUser
import com.kamel.practice.model.ChatMessage
import com.kamel.practice.model.ChatRoom
import com.kamel.practice.repository.ActiveUserRepository
import com.kamel.practice.repository.ChatMessageRepository
import com.kamel.practice.repository.ChatRoomRepository
import com.kamel.practice.util.GameAlreadyExistsException
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Service layer for chat operations
 * Implements business logic and coordinates between repositories
 */
@Service
@Transactional
class ChatService(
    private val messageRepository: ChatMessageRepository,
    private val roomRepository: ChatRoomRepository,
    private val activeUserRepository: ActiveUserRepository
) {

    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    /**
     * Save a chat message to database
     */
    fun saveMessage(messageRequest: MessageRequestDto): ChatMessage {
        val message = ChatMessage(
            content = messageRequest.content,
            sender = messageRequest.sender,
            messageType = messageRequest.type,
            roomId = messageRequest.roomId
        )

        logger.debug("Saving message from ${messageRequest.sender} in room ${messageRequest.roomId}")
        return messageRepository.save(message)
    }

    /**
     * Get recent messages for a room
     */
    fun getRecentMessages(roomId: String, limit: Int = 50): List<MessageResponseDto> {
        logger.debug("Fetching recent messages for room: $roomId")
        return messageRepository.findByRoomIdOrderByTimestampDesc(roomId)
            .take(limit)
            .reversed() // Return in chronological order
            .map { MessageResponseDto.from(it) }
    }

    /**
     * Get messages after a specific timestamp
     */
    fun getMessagesAfter(roomId: String, timestamp: LocalDateTime): List<MessageResponseDto> {
        return messageRepository.findByRoomIdAndTimestampAfterOrderByTimestampAsc(roomId, timestamp)
            .map { MessageResponseDto.from(it) }
    }

    /**
     * Create a new chat room
     */
    fun createRoom(name: String, description: String?, createdBy: String): ChatRoom {
        if (roomRepository.existsByNameIgnoreCase(name)) {
            throw GameAlreadyExistsException("Room with name '$name' already exists")
        }

        val room = ChatRoom(
            name = name,
            description = description,
            createdBy = createdBy,
            code = name.lowercase().replace(" ", "-") // Simple code generation
        )

        logger.info("Creating new room: $name by $createdBy")
        return roomRepository.save(room)
    }

    /**
     * Get all active rooms
     */
    fun getActiveRooms(): List<RoomResponseDto> {
        return roomRepository.findByIsActiveTrue().map { room ->
            val activeUsers = activeUserRepository.countByRoomId(room.id.toHexString())
            RoomResponseDto(
                id = room.id.toHexString(),
                name = room.name,
                description = room.description,
                activeUsers = activeUsers.toInt(),
                createdAt = room.createdAt,
                code = room.code,
                isActive = room.isActive
            )
        }
    }

    /**
     * Get room by ID
     */
    fun getRoomById(roomId: String): ChatRoom? {
        return roomRepository.findById(ObjectId(roomId)).orElse(null)
    }

    /**
     * Add user to room
     */
    fun addUserToRoom(username: String, roomId: String): ActiveUser {
        // Remove user from room if already exists (handles reconnections)
        activeUserRepository.deleteByUsernameAndRoomId(username, roomId)

        val activeUser = ActiveUser(
            userName = username,
            roomId = roomId
        )

        logger.info("User $username joined room $roomId")
        return activeUserRepository.save(activeUser)
    }

    /**
     * Remove user from room
     */
    fun removeUserFromRoom(username: String, roomId: String) {
        logger.info("User $username left room $roomId")
        activeUserRepository.deleteByUsernameAndRoomId(username, roomId)
    }

    /**
     * Get active users in a room
     */
    fun getActiveUsersInRoom(roomId: String): List<String> {
        return activeUserRepository.findByRoomId(roomId)
            .map { it.userName }
    }

    /**
     * Update user's last seen timestamp
     */
    fun updateUserLastSeen(username: String, roomId: String) {
        activeUserRepository.findByUsernameAndRoomId(username, roomId)?.let { user ->
            val updatedUser = user.copy(lastSeen = LocalDateTime.now())
            activeUserRepository.save(updatedUser)
        }
    }

    /**
     * Clean up inactive users (called periodically)
     */
    fun cleanupInactiveUsers(inactiveThresholdMinutes: Long = 30) {
        val threshold = LocalDateTime.now().minusMinutes(inactiveThresholdMinutes)
        val inactiveUsers = activeUserRepository.findByLastSeenBefore(threshold)

        if (inactiveUsers.isNotEmpty()) {
            logger.info("Cleaning up ${inactiveUsers.size} inactive users")
            activeUserRepository.deleteByLastSeenBefore(threshold)
        }
    }

    /**
     * Get message count for a room
     */
    fun getMessageCount(roomId: String): Long {
        return messageRepository.countByRoomId(roomId)
    }
}