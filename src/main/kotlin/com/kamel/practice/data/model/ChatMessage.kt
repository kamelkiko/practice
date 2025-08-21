package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("messages")
data class ChatMessage(
    @Id
    val id: ObjectId = ObjectId.get(),
    val roomId: String,
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: MessageType = MessageType.CHAT
) {
    enum class MessageType {
        CHAT, JOIN, LEAVE
    }
}