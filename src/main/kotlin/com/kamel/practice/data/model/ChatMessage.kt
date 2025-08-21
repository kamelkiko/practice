package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("messages")
data class ChatMessage(
    @Id
    val id: ObjectId = ObjectId.get(),
    val roomId: String,
    val senderId: String,
    val content: String,
    val timestamp: Instant = Instant.now(),
    val messageType: MessageType,
) {
    enum class MessageType {
        CHAT, JOIN, LEAVE
    }
}