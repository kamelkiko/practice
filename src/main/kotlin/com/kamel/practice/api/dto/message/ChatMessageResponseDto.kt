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
    val senderUsername: String? = null,
    val senderProfilePictureUrl: String? = null,
    val messageType: ChatMessage.MessageType,
)

fun ChatMessage.toDto(): ChatMessageResponseDto {
    return ChatMessageResponseDto(
        id = id.toHexString(),
        roomId = roomId.toHexString(),
        senderId = senderId.toHexString(),
        content = content,
        timestamp = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault()),
        senderUsername = "",
        senderProfilePictureUrl = "",
        messageType = messageType
    )
}