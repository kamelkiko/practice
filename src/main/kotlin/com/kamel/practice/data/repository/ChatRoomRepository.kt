package com.kamel.practice.data.repository

import com.kamel.practice.data.model.ChatRoom
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRoomRepository : MongoRepository<ChatRoom, ObjectId> {
    fun findByCreatedByOrderByCreatedAtDesc(createdBy: String): List<ChatRoom>
    fun findAllByIsActiveOrderByCreatedAtDesc(isActive: Boolean): List<ChatRoom>
    fun existsByName(name: String): Boolean
}