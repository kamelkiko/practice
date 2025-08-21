package com.kamel.practice.domain.service.room

import com.kamel.practice.api.dto.room.ChatRoomResponseDto
import com.kamel.practice.api.dto.room.toDto
import com.kamel.practice.data.model.ChatRoom
import com.kamel.practice.data.repository.ChatRoomRepository
import com.kamel.practice.data.repository.UserRepository
import com.kamel.practice.domain.exception.ChatAlreadyExistsException
import com.kamel.practice.domain.exception.ChatNotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class ChatRoomService(
    private val chatRoomRepository: ChatRoomRepository,
    private val userRepository: UserRepository,
) {
    fun getAllActiveRooms(): List<ChatRoomResponseDto> {
        return chatRoomRepository.findAllByIsActiveOrderByCreatedAtDesc(true).map { it.toDtoWithOwner() }
    }

    fun getRoomById(roomId: String): ChatRoomResponseDto {
        return chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }.toDtoWithOwner()
    }

    fun createRoom(name: String, description: String?, createdBy: String, isActive: Boolean?): ChatRoomResponseDto {
        if (chatRoomRepository.existsByName(name)) {
            throw ChatAlreadyExistsException("Chat room with name $name already exists")
        }
        val room = chatRoomRepository.save(
            ChatRoom(
                name = name,
                description = description,
                createdBy = ObjectId(createdBy),
                isActive = isActive ?: true
            )
        )
        return room.toDtoWithOwner()
    }

    fun updateRoom(roomId: String, name: String?, description: String?, isActive: Boolean?): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }

        if (name != null && chatRoomRepository.existsByName(name) && room.name != name) {
            throw ChatAlreadyExistsException("Chat room with name $name already exists")
        }

        val updatedRoom = room.copy(
            name = name ?: room.name,
            description = description ?: room.description,
            isActive = isActive ?: room.isActive
        )

        return chatRoomRepository.save(updatedRoom).toDtoWithOwner()
    }

    fun deleteRoom(roomId: String): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            throw ChatNotFoundException("Chat room with ID $roomId not found")
        }

        chatRoomRepository.delete(room)
        return room.toDto()
    }

    fun getRoomsByUserId(userId: String): List<ChatRoomResponseDto> {
        return chatRoomRepository.findByCreatedByOrderByCreatedAtDesc(userId).map { it.toDtoWithOwner() }
    }

    fun addPicture(
        roomId: String,
        pictureUrl: String
    ): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            ChatNotFoundException("Chat room not found with ID: $roomId")
        }
        val updatedRoom = room.copy(pictureUrl = pictureUrl)
        return chatRoomRepository.save(updatedRoom).toDtoWithOwner()
    }

    fun deletePicture(roomId: String): ChatRoomResponseDto {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            ChatNotFoundException("Chat room not found with ID: $roomId")
        }
        if (room.pictureUrl == null || room.pictureUrl.isEmpty()) {
            throw ChatNotFoundException("Chat room does not have a picture to delete")
        }
        val updatedRoom = room.copy(pictureUrl = null)
        return chatRoomRepository.save(updatedRoom).toDtoWithOwner()
    }

    fun downloadRoomPicture(roomId: String): String {
        val room = chatRoomRepository.findById(ObjectId(roomId)).orElseThrow {
            ChatNotFoundException("Chat room not found with ID: $roomId")
        }
        if (room.pictureUrl == null || room.pictureUrl.isEmpty()) {
            throw ChatNotFoundException("Chat room does not have a picture")
        }
        return room.id.toHexString()
    }

    private fun ChatRoom.toDtoWithOwner(): ChatRoomResponseDto {
        val user = userRepository.findById(createdBy).orElseThrow {
            ChatNotFoundException("User with ID $createdBy not found")
        }
        return ChatRoomResponseDto(
            id = id.toHexString(),
            name = name,
            description = description,
            pictureUrl = pictureUrl,
            createdBy = createdBy.toHexString(),
            owner = user?.username ?: "Unknown",
            createdAt = LocalDateTime.ofInstant(createdAt, ZoneId.systemDefault()),
            isActive = isActive
        )
    }
}