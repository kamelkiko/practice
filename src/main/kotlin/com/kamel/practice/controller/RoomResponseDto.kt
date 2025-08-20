package com.kamel.practice.controller

import java.time.LocalDateTime

data class RoomResponseDto(
    val id: String,
    val name: String,
    val code: String,
    val description: String?,
    val activeUsers: Int,
    val createdAt: LocalDateTime,
    val isActive: Boolean,
)