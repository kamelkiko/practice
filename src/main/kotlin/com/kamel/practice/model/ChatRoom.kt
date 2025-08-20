package com.kamel.practice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("rooms")
data class ChatRoom(
    @Id
    val id: ObjectId = ObjectId.get(),
    val code: String,
    val name: String,
    val description: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val createdBy: String,
    val isActive: Boolean = true,
    //val pictureUrl: String? = null,
)