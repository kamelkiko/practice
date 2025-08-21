package com.kamel.practice.api.dto.message

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class ChatMessageRequestDto(
    @field:NotBlank(message = "RoomID cannot be blank")
    val roomId: String,
    @field:NotBlank(message = "SenderID cannot be blank")
    val senderId: String,
    @field:NotBlank(message = "Content cannot be blank")
    @field:Length(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    val content: String,
)