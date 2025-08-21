package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("User")
data class User(
    @Id
    val id: ObjectId = ObjectId.get(),
    val code: String,
    val username: String,
    val email: String,
    val password: String,
    val createdAt: Instant = Instant.now(),
    val profilePictureUrl: String? = null,
    val status: Status,
    //val phoneNumber: String? = null,
    //val bio: String? = null,
) {
    enum class Status {
        ONLINE, OFFLINE
    }
}