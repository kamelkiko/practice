package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.ServerResponse
import com.kamel.practice.api.dto.message.ChatMessageRequestDto
import com.kamel.practice.api.dto.message.ChatMessageResponseDto
import com.kamel.practice.data.model.ChatMessage
import com.kamel.practice.domain.service.message.ChatMessageService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/messages")
class ChatMessageController(
    private val chatMessageService: ChatMessageService,
    private val messagingTemplate: SimpMessagingTemplate
) {
    @MessageMapping("topic/chat.sendMessage")
    @SendTo("/topic/chat")
    fun sendMessage(
        @Payload messageRequest: ChatMessageRequestDto
    ): ChatMessageResponseDto {
        return chatMessageService.saveMessage(
            roomId = messageRequest.roomId,
            content = messageRequest.content,
            type = ChatMessage.MessageType.CHAT,
            senderId = messageRequest.senderId
        )
    }

    @GetMapping("/{roomId}")
    fun getMessagesByRoomId(
        @PathVariable roomId: String
    ) = ServerResponse.success(
        data = chatMessageService.getMessagesByRoomId(roomId),
        successMessage = "Messages retrieved successfully",
    )
}