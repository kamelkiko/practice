package com.kamel.practice.config

import com.kamel.practice.domain.service.room.RoomUserService
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent

@Component
class WebSocketEventHandler(
    private val roomUserService: RoomUserService,
) {
    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectEvent) {
        println("Received a new web socket connection: ${event.message}")
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent, headerAccessor: StompHeaderAccessor) {
        val userId = headerAccessor.sessionAttributes?.get("userId") as String?
        val roomId = headerAccessor.sessionAttributes?.get("roomId") as String?
        if (userId != null && roomId != null) {
            roomUserService.removeUserFromRoom(roomId, userId)
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