package com.kamel.practice.controller

import com.kamel.practice.model.MessageType

data class MessageRequestDto(
    val content: String,
    val sender: String,
    val roomId: String,
    val type: MessageType = MessageType.CHAT
)