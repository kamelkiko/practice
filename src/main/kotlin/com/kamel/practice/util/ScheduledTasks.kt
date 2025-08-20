package com.kamel.practice.util

import com.kamel.practice.service.ChatService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Scheduled tasks for maintenance operations
 */
@Component
@EnableScheduling
class ScheduledTasks(
    private val chatService: ChatService
) {

    private val logger = LoggerFactory.getLogger(ScheduledTasks::class.java)

    /**
     * Clean up inactive users every 10 minutes
     * Users inactive for more than 30 minutes are removed
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    fun cleanupInactiveUsers() {
        try {
            logger.debug("Running scheduled cleanup of inactive users")
            chatService.cleanupInactiveUsers(inactiveThresholdMinutes = 30)
        } catch (e: Exception) {
            logger.error("Error during scheduled cleanup: ${e.message}", e)
        }
    }

    /**
     * Log system health every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    fun logSystemHealth() {
        try {
            val activeRooms = chatService.getActiveRooms()
            val totalActiveUsers = activeRooms.sumOf { it.activeUsers }

            logger.info("System Health - Active Rooms: ${activeRooms.size}, Total Active Users: $totalActiveUsers")
        } catch (e: Exception) {
            logger.error("Error during health check: ${e.message}", e)
        }
    }
}