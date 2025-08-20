package com.kamel.practice.repository

import com.kamel.practice.model.ActiveUser
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActiveUserRepository : MongoRepository<ActiveUser, ObjectId> {

    /**
     * Find active users in a room
     */
    fun findByRoomId(roomId: String): List<ActiveUser>

    /**
     * Find user by username and room
     */
    fun findByUsernameAndRoomId(username: String, roomId: String): ActiveUser?

    /**
     * Remove user from room
     */
    fun deleteByUsernameAndRoomId(username: String, roomId: String)

    /**
     * Count active users in a room
     */
    fun countByRoomId(roomId: String): Long

    /**
     * Find users who haven't been seen recently (for cleanup)
     */
    fun findByLastSeenBefore(threshold: LocalDateTime): List<ActiveUser>

    /**
     * Remove inactive users
     */
    fun deleteByLastSeenBefore(threshold: LocalDateTime)
}