package com.kamel.practice.api.dto.message

import com.kamel.practice.data.model.ChatMessage
import java.time.LocalTime

data class ChatMessageResponseDto(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val timestamp: LocalTime,
    val senderUsername: String,
    val senderProfilePictureUrl: String? = null
)

fun ChatMessage.toDto(): ChatMessageResponseDto {
    return ChatMessageResponseDto(
        id = id.toHexString(),
        roomId = roomId,
        senderId = senderId,
        content = content,
        timestamp = LocalTime.ofNanoOfDay(timestamp.toEpochMilli() * 1_000_000),
        senderUsername = "",
        senderProfilePictureUrl = ""
    )
}