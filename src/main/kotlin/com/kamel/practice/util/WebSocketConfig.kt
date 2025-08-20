// WebSocketConfig.kt
package com.kamel.practice.util

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker options
     * - /topic for broadcasting messages to multiple subscribers
     * - /queue for point-to-point messaging
     */
    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enable simple broker for destinations prefixed with /topic and /queue
        config.enableSimpleBroker("/topic", "/queue")

        // Set application destination prefix for messages bound for @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app")

        // Set user destination prefix for user-specific destinations
        config.setUserDestinationPrefix("/user")
    }

    /**
     * Register STOMP endpoints mapping to specific URLs
     * SockJS fallback options are enabled for browsers that don't support WebSocket
     */
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
//            .setAllowedOriginPatterns("*") // Configure properly for production
//            .withSockJS()
//            .setHeartbeatTime(25000) // Send heartbeat every 25 seconds
//            .setDisconnectDelay(30000) // Delay before considering connection lost
    }
}