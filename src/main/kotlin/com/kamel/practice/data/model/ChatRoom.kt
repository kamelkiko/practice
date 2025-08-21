package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("rooms")
data class ChatRoom(
    @Id
    val id: ObjectId = ObjectId.get(),
    val name: String,
    val description: String? = null,
    val pictureUrl: String? = null,
    val createdBy: ObjectId,
    val createdAt: Instant = Instant.now(),
    val isActive: Boolean,
)