package com.kamel.practice.api.dto

import com.kamel.practice.data.model.User
import java.time.LocalDateTime

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: LocalDateTime,
    val status: User.Status,
    val profilePictureUrl: String? = null,
)

fun User.toDto(): UserDto {
    return UserDto(
        id = id.toHexString(),
        username = username,
        email = email,
        createdAt = LocalDateTime.ofInstant(createdAt, java.time.ZoneId.systemDefault()),
        status = status,
        profilePictureUrl = profilePictureUrl
    )
}