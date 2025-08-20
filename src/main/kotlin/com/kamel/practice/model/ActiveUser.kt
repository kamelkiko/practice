package com.kamel.practice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("active_users")
data class ActiveUser(
    @Id
    val id: ObjectId = ObjectId.get(),
    val userName: String,
    val roomId: String,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
    val lastSeen: LocalDateTime = LocalDateTime.now()
)

data class ActiveUserDetails(
    @Id
    val id: ObjectId = ObjectId.get(),
    val userId: String,
    val userName: String,
    //val userPictureUrl: String? = null,
    val roomId: String,
    val joinedAt: LocalDateTime = LocalDateTime.now(),
    val lastSeen: LocalDateTime = LocalDateTime.now()
)