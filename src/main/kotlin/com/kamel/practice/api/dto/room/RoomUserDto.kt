package com.kamel.practice.api.dto.room

import java.time.LocalDateTime

data class RoomUserDto(
    val roomId: String,
    val userId: String,
    val id: String,
    val username: String,
    val avatar: String? = null,
    val lastSeen: Long,
    val joinedAt: LocalDateTime,
)