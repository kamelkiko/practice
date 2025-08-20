package com.kamel.practice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("users")
data class User(
    @Id
    val id: ObjectId = ObjectId.get(),
    val username: String,
    val password: String,
    //val email: String, // to send otp
    //val phoneNumber: String? = null,
//    val firstName: String,
//    val lastName: String? = null,
    // val pictureUrl: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)