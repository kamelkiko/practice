package com.kamel.practice.api.dto.message

import com.kamel.practice.data.model.ChatMessage
import java.time.LocalDateTime
import java.time.ZoneId

data class ChatMessageResponseDto(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val timestamp: LocalDateTime,
    val senderUsername: String,
    val senderProfilePictureUrl: String? = null
)

fun ChatMessage.toDto(): ChatMessageResponseDto {
    return ChatMessageResponseDto(
        id = id.toHexString(),
        roomId = roomId,
        senderId = senderId,
        content = content,
        timestamp = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()),
        senderUsername = "",
        senderProfilePictureUrl = ""
    )
}