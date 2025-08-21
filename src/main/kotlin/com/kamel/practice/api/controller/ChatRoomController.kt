package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.ServerResponse
import com.kamel.practice.api.dto.message.ChatMessageResponseDto
import com.kamel.practice.api.dto.room.ChatRoomEventDto
import com.kamel.practice.api.dto.room.ChatRoomRequestDto
import com.kamel.practice.api.dto.room.ChatRoomResponseDto
import com.kamel.practice.api.dto.room.UserJoinRoomDto
import com.kamel.practice.data.model.ChatMessage
import com.kamel.practice.data.model.ImageMetadata
import com.kamel.practice.domain.exception.ChatException
import com.kamel.practice.domain.service.room.ChatRoomService
import com.kamel.practice.domain.service.room.RoomUserService
import com.kamel.practice.domain.service.storage.ImageService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/rooms")
class ChatRoomController(
    private val chatRoomService: ChatRoomService,
    private val roomUserService: RoomUserService,
    private val imageService: ImageService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/topic/room.addUser")
    @SendTo("/topic/room")
    fun addUserToRoom(
        @Payload userJoinRoomDto: UserJoinRoomDto,
    ): ChatMessageResponseDto {
        return roomUserService.addUserToRoom(
            userId = userJoinRoomDto.userId,
            roomId = userJoinRoomDto.roomId,
        ).let { roomUser ->
            ChatMessageResponseDto(
                roomId = roomUser.roomId,
                senderId = roomUser.userId,
                content = "${roomUser.username} has joined the room",
                timestamp = LocalDateTime.now(),
                messageType = ChatMessage.MessageType.JOIN,
                senderUsername = roomUser.username,
                senderProfilePictureUrl = roomUser.avatar,
                id = roomUser.id
            )
        }
    }

    @MessageMapping("/topic/room.removeUser")
    @SendTo("/topic/room")
    fun removeUserFromRoom(
        @Payload userJoinRoomDto: UserJoinRoomDto,
    ): ChatMessageResponseDto {
        val removedUser = roomUserService.removeUserFromRoom(
            userId = userJoinRoomDto.userId,
            roomId = userJoinRoomDto.roomId,
        )
        return ChatMessageResponseDto(
            roomId = userJoinRoomDto.roomId,
            senderId = userJoinRoomDto.userId,
            content = "${removedUser.username} has left the room",
            timestamp = LocalDateTime.now(),
            messageType = ChatMessage.MessageType.LEAVE,
            senderUsername = removedUser.username,
            senderProfilePictureUrl = removedUser.username,
            id = removedUser.id
        )
    }

    @GetMapping("/{roomId}/users")
    fun getUsersInRoom(
        @PathVariable roomId: String,
    ) = ServerResponse.success(
        data = roomUserService.getRoomUsersByRoomId(roomId),
        successMessage = "Users in room retrieved successfully",
    )

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRoom(
        @Valid @RequestBody room: ChatRoomRequestDto
    ) = ServerResponse.success(
        data = chatRoomService.createRoom(
            name = room.name,
            description = room.description,
            isActive = room.isActive,
            createdBy = room.createdBy
        ),
        successMessage = "Room created successfully",
        code = HttpStatus.CREATED.value(),
    ).also {
        if (it.data?.isActive == true) {
            messagingTemplate.convertAndSend(
                "/topic/room",
                ChatRoomEventDto(
                    type = ChatRoomEventDto.RoomEventType.ADD,
                    data = it.data
                )
            )
        }
    }

    @PutMapping("/{roomId}")
    fun updateRoom(
        @Valid @RequestBody room: ChatRoomRequestDto,
        @PathVariable roomId: String,
    ) = ServerResponse.success(
        data = chatRoomService.updateRoom(
            roomId = roomId,
            name = room.name,
            description = room.description,
            isActive = room.isActive,
        ),
        successMessage = "Room updated successfully",
    ).also {
        if (it.data?.isActive == true) {
            messagingTemplate.convertAndSend(
                "/topic/room",
                ChatRoomEventDto(
                    type = ChatRoomEventDto.RoomEventType.UPDATE,
                    data = it.data
                )
            )
        } else {
            messagingTemplate.convertAndSend(
                "/topic/room",
                ChatRoomEventDto(
                    type = ChatRoomEventDto.RoomEventType.DELETE,
                    data = it.data
                )
            )
        }
    }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(
        @PathVariable roomId: String,
    ) = ServerResponse.success(
        data = chatRoomService.deleteRoom(roomId),
        successMessage = "Room deleted successfully",
    ).also {
        if (it.data?.isActive == true) {
            messagingTemplate.convertAndSend(
                "/topic/room",
                ChatRoomEventDto(
                    type = ChatRoomEventDto.RoomEventType.DELETE,
                    data = it.data
                )
            )
        }
    }

    @GetMapping("/{roomId}")
    fun getRoomById(
        @PathVariable roomId: String,
    ) = ServerResponse.success(
        data = chatRoomService.getRoomById(roomId),
        successMessage = "Room retrieved successfully",
    )

    @GetMapping
    fun getAllRooms() = ServerResponse.success(
        data = chatRoomService.getAllActiveRooms(),
        successMessage = "Rooms retrieved successfully",
    )

    @GetMapping("owner/{ownerId}")
    fun getRoomsByOwner(
        @PathVariable ownerId: String,
    ) = ServerResponse.success(
        data = chatRoomService.getRoomsByUserId(ownerId),
        successMessage = "Rooms by owner retrieved successfully",
    )

    @PostMapping("/image/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    fun uploadRoomImage(
        @RequestParam roomId: String,
        @RequestParam("file") file: MultipartFile?,
    ): ServerResponse<ChatRoomResponseDto> {
        if (file == null || file.isEmpty) {
            throw ChatException("File is empty or not provided.")
        }
        val imageMetaData = file.let {
            imageService.uploadImage(it, roomId, ImageMetadata.ImageType.ROOM)
        }
        return ServerResponse.success(
            data = chatRoomService.addPicture(roomId, imageMetaData.storedName),
            successMessage = "Room image uploaded successfully",
        ).also {
            if (it.data?.isActive == true) {
                messagingTemplate.convertAndSend(
                    "/topic/room",
                    ChatRoomEventDto(
                        type = ChatRoomEventDto.RoomEventType.UPDATE,
                        data = it.data
                    )
                )
            }
        }
    }

    @PatchMapping("/image/replace", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    fun replaceRoomImage(
        @RequestParam roomId: String,
        @RequestParam file: MultipartFile?
    ): ServerResponse<ChatRoomResponseDto> {
        if (file == null || file.isEmpty) {
            throw ChatException("File is empty or not provided.")
        }
        val imageMetaData = file.let {
            imageService.replaceImage(it, roomId, ImageMetadata.ImageType.ROOM)
        }
        return ServerResponse.success(
            data = chatRoomService.addPicture(roomId, imageMetaData.storedName),
            successMessage = "Room image replaced successfully",
        ).also {
            if (it.data?.isActive == true) {
                messagingTemplate.convertAndSend(
                    "/topic/room",
                    ChatRoomEventDto(
                        type = ChatRoomEventDto.RoomEventType.UPDATE,
                        data = it.data
                    )
                )
            }
        }
    }

    @GetMapping("/image/download")
    fun downloadRoomImage(
        @RequestParam roomId: String,
        response: HttpServletResponse,
    ): Resource {
        val ownerId = chatRoomService.downloadRoomPicture(roomId)
        val metadata = imageService.getImageMetadata(ownerId, ImageMetadata.ImageType.ROOM)
        val resource = imageService.getImageResource(ownerId, ImageMetadata.ImageType.ROOM)
        response.contentType = metadata.mimeType
        response.addHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + metadata.originalName + "\""
        )
        response.setContentLengthLong(metadata.size)
        return resource
    }

    @DeleteMapping("/image/delete")
    @Transactional
    fun deleteRoomImage(
        @RequestParam roomId: String
    ): ServerResponse<ChatRoomResponseDto> {
        val room = chatRoomService.deletePicture(roomId)
        imageService.deleteImage(roomId, ImageMetadata.ImageType.ROOM)
        return ServerResponse.success(
            data = room,
            successMessage = "Room image deleted successfully",
        ).also {
            if (it.data?.isActive == true) {
                messagingTemplate.convertAndSend(
                    "/topic/room",
                    ChatRoomEventDto(
                        type = ChatRoomEventDto.RoomEventType.UPDATE,
                        data = it.data.copy(description = "deleted")
                    )
                )
            }
        }
    }
}