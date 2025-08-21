package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("room_users")
data class RoomUser(
    @Id
    val id: ObjectId = ObjectId.get(),
    val roomId: ObjectId,
    val userId: ObjectId,
    val lastSeen: Long = System.currentTimeMillis(),
    val joinedAt: Instant = Instant.now(),
)