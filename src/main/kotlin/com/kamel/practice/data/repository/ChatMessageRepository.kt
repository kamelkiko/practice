package com.kamel.practice.data.repository

import com.kamel.practice.data.model.ChatMessage
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : MongoRepository<ChatMessage, ObjectId> {
}