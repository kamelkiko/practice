package com.kamel.practice.repository

import com.kamel.practice.model.ChatMessage
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, ObjectId> {

    /**
     * Find messages by room ID, ordered by timestamp descending
     */
    fun findByRoomIdOrderByTimestampDesc(roomId: String): List<ChatMessage>

    /**
     * Find recent messages in a room (limit results)
     */
    @Query("{ 'roomId': ?0 }")
    fun findRecentMessagesByRoomId(roomId: String, limit: Int = 50): List<ChatMessage>

    /**
     * Find messages by sender in a specific room
     */
    fun findByRoomIdAndSenderOrderByTimestampDesc(roomId: String, sender: String): List<ChatMessage>

    /**
     * Count messages in a room
     */
    fun countByRoomId(roomId: String): Long

    /**
     * Find messages after a specific timestamp
     */
    fun findByRoomIdAndTimestampAfterOrderByTimestampAsc(
        roomId: String,
        timestamp: LocalDateTime
    ): List<ChatMessage>
}