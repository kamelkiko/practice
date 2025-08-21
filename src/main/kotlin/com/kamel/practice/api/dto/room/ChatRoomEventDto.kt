package com.kamel.practice.api.dto.room

data class ChatRoomEventDto<T>(
    val type: RoomEventType,
    val data: T
) {
    enum class RoomEventType {
        ADD, UPDATE, DELETE
    }
}