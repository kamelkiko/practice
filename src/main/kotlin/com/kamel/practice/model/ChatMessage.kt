package com.kamel.practice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("messages")
data class ChatMessage(
    @Id
    val id: ObjectId = ObjectId.get(),
    val roomId: String,
    val sender: String,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val messageType: MessageType,
)

enum class MessageType {
    CHAT, JOIN, LEAVE
}