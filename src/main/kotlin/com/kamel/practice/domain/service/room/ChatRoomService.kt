package com.kamel.practice.domain.service.room

import com.kamel.practice.api.dto.room.ChatRoomResponseDto
import com.kamel.practice.api.dto.room.toDto
import com.kamel.practice.data.model.ChatRoom
import com.kamel.practice.data.repository.ChatRoomRepository
import com.kamel.practice.domain.exception.ChatNotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository
) {
    fun getAllActiveRooms(): List<ChatRoomResponseDto> {
        return chatRoomRepository.findAllByIsActiveOrderByCreatedAtDesc(true).map { it.toDto() }
    }

    fun getRoomById(roomId: String): ChatRoomResponseDto {
        return chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }.toDto()
    }

    fun createRoom(name: String, description: String?, createdBy: String): ChatRoomResponseDto {
        val room = chatRoomRepository.save(
            ChatRoom(
                name = name,
                description = description,
                createdBy = createdBy,
                isActive = true
            )
        )
        return room.toDto()
    }

    fun updateRoom(roomId: String, name: String?, description: String?): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }

        val updatedRoom = room.copy(
            name = name ?: room.name,
            description = description ?: room.description
        )

        return chatRoomRepository.save(updatedRoom).toDto()
    }

    fun deleteRoom(roomId: String): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }

        chatRoomRepository.delete(room)
        return room.toDto()
    }

    fun getRoomsByUserId(userId: String): List<ChatRoomResponseDto> {
        return chatRoomRepository.findByCreatedByOrderByCreatedAtDesc(userId).map { it.toDto() }
    }
}