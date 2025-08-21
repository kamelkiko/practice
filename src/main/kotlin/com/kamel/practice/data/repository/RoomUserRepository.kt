package com.kamel.practice.data.repository

import com.kamel.practice.data.model.RoomUser
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomUserRepository : MongoRepository<RoomUser, ObjectId> {
    fun findByRoomId(roomId: ObjectId): List<RoomUser>
    fun existsByRoomIdAndUserId(roomId: ObjectId, userId: ObjectId): Boolean
    fun deleteByRoomIdAndUserId(roomId: ObjectId, userId: ObjectId): Long
}