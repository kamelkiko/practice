package com.kamel.practice.repository

import com.kamel.practice.model.ChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : MongoRepository<ChatRoom, ObjectId> {

    /**
     * Find active rooms
     */
    fun findByIsActiveTrue(): List<ChatRoom>

    /**
     * Find rooms by creator
     */
    fun findByCreatedByOrderByCreatedAtDesc(createdBy: String): List<ChatRoom>

    /**
     * Find room by name (case-insensitive)
     */
    @Query($$"{ 'name': { $regex: ?0, $options: 'i' } }")
    fun findByNameIgnoreCase(name: String): ChatRoom?

    /**
     * Check if room exists by name
     */
    fun existsByNameIgnoreCase(name: String): Boolean
}