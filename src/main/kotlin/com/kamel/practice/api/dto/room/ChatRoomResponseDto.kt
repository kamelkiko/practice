package com.kamel.practice.api.dto.room

import com.kamel.practice.data.model.ChatRoom
import java.time.LocalDateTime

data class ChatRoomResponseDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val pictureUrl: String? = null,
    val createdBy: String,
    val owner: String,
    val createdAt: LocalDateTime,
    val isActive: Boolean,
)

fun ChatRoom.toDto(): ChatRoomResponseDto {
    return ChatRoomResponseDto(
        id = id.toHexString(),
        name = name,
        description = description,
        pictureUrl = pictureUrl,
        createdBy = createdBy,
        createdAt = LocalDateTime.ofInstant(createdAt, java.time.ZoneId.systemDefault()),
        isActive = isActive,
        owner = ""
    )
}