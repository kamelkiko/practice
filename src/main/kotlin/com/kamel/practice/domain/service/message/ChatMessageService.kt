package com.kamel.practice.domain.service.message

import com.kamel.practice.api.dto.message.toDto
import com.kamel.practice.data.model.ChatMessage
import com.kamel.practice.data.repository.ChatMessageRepository
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository
) {
    fun getMessagesByRoomId(roomId: String) =
        chatMessageRepository.findByRoomIdOrderByTimestampDesc(roomId).map { it.toDto() }

    fun getMessagesByRoomIdAndSenderId(roomId: String, senderId: String) =
        chatMessageRepository.findByRoomIdAndSenderIdOrderByTimestampDesc(roomId, senderId).map { it.toDto() }

    fun saveMessage(roomId: String, senderId: String, content: String, type: ChatMessage.MessageType) =
        chatMessageRepository.save(
            ChatMessage(
                roomId = roomId,
                senderId = senderId,
                content = content,
                messageType = type
            )
        ).toDto()
}