package com.kamel.practice.config

import com.kamel.practice.api.dto.message.ChatMessageResponseDto
import com.kamel.practice.data.model.ChatMessage
import com.kamel.practice.domain.service.room.RoomUserService
import com.kamel.practice.domain.service.user.UserService
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import java.time.LocalDateTime


@Component
class WebSocketEventHandler(
    private val roomUserService: RoomUserService,
    private val userService: UserService,
    private val messagingTemplate: SimpMessageSendingOperations,
) {
    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectEvent) {
        println("Received a new web socket connection: ${event.message}")
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor: StompHeaderAccessor = StompHeaderAccessor.wrap(event.message)
        val userId = headerAccessor.sessionAttributes?.get("userId") as String?
        val roomId = headerAccessor.sessionAttributes?.get("roomId") as String?
        if (userId != null && roomId != null) {
            val user = roomUserService.removeUserFromRoom(roomId, userId)
            messagingTemplate.convertAndSend(
                "/topic/room",
                ChatMessageResponseDto(
                    roomId = roomId,
                    senderId = userId,
                    content = "${user.username} has left the room",
                    timestamp = LocalDateTime.now(),
                    messageType = ChatMessage.MessageType.LEAVE,
                    senderUsername = user.username,
                    senderProfilePictureUrl = user.avatar,
                    id = roomId
                )
            )
            headerAccessor.sessionAttributes?.remove("userId", userId)
            headerAccessor.sessionAttributes?.remove("roomId", roomId)
        }
        println("Client disconnected: ${event.message}")
    }

    @EventListener
    fun handleWebSocketSubscribeListener(event: SessionSubscribeEvent) {
        println("Client subscribed: ${event.message}")
    }

    @EventListener
    fun handleBrokerAvailabilityEvent(event: BrokerAvailabilityEvent) {
        println("Broker availability changed: ${event.isBrokerAvailable}")
    }
}