package com.kamel.practice.controller

import com.kamel.practice.model.ChatMessage
import com.kamel.practice.model.MessageType
import java.time.LocalDateTime

data class MessageResponseDto(
    val id: String,
    val content: String,
    val sender: String,
    val type: MessageType,
    val roomId: String,
    val timestamp: LocalDateTime
) {
    companion object {
        fun from(message: ChatMessage): MessageResponseDto {
            return MessageResponseDto(
                id = message.id.toHexString() ?: "",
                content = message.content,
                sender = message.sender,
                type = message.messageType,
                roomId = message.roomId,
                timestamp = message.timestamp
            )
        }
    }
}