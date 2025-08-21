package com.kamel.practice.config

import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.broker.BrokerAvailabilityEvent
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent

@Component
class WebSocketEventHandler() {
    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectEvent) {
        println("Received a new web socket connection: ${event.message}")
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        println("Web socket disconnected: ${event.message}")
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