package com.kamel.practice.api.dto.room

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

data class ChatRoomRequestDto(
    @field:NotBlank(message = "Name cannot be blank")
    @field:Length(min = 3, max = 50, message = "Name must not exceed 50 characters")
    val name: String,
    @field:NotBlank(message = "Name cannot be blank")
    @field:Length(min = 10, max = 100, message = "description must be between 10 and 100 characters")
    val description: String? = null,
    val isActive: Boolean = true,
)