package com.kamel.practice.domain.service.room

import com.kamel.practice.api.dto.room.RoomUserDto
import com.kamel.practice.data.model.RoomUser
import com.kamel.practice.data.repository.RoomUserRepository
import com.kamel.practice.domain.exception.ChatAlreadyExistsException
import com.kamel.practice.domain.exception.ChatNotFoundException
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomUserService(
    private val roomUserRepository: RoomUserRepository,
    private val mongoTemplate: MongoTemplate,
) {
    fun getRoomUsersByRoomId(roomId: String): List<RoomUserDto> {
        val lookup = LookupOperation.newLookup()
            .from("User")
            .localField("userId")
            .foreignField("_id")
            .`as`("user")

        val aggregation = newAggregation(
            match(Criteria.where("roomId").`is`(ObjectId(roomId))),
            lookup,
            unwind("user", true),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "joinedAt"),
            project("roomId", "userId", "lastSeen", "joinedAt")
                .and("_id").`as`("id")
                .and("user.username").`as`("username")
                .and("user.profilePictureUrl").`as`("avatar")
        )

        return mongoTemplate.aggregate(
            aggregation,
            "room_users",
            RoomUserDto::class.java
        ).mappedResults
    }

    fun isUserInRoom(roomId: String, userId: String): Boolean {
        return roomUserRepository.existsByRoomIdAndUserId(
            ObjectId(roomId),
            ObjectId(userId)
        )
    }

    @Transactional
    fun removeUserFromRoom(roomId: String, userId: String): RoomUserDto {
        val aggregation = newAggregation(
            match(Criteria.where("roomId").`is`(ObjectId(roomId)).and("userId").`is`(ObjectId(userId))),
            lookup(
                "User",
                "userId",
                "_id",
                "user"
            ),
            unwind("user"),
            project()
                .andExpression("_id").`as`("id")
                .andExpression("roomId").`as`("roomId")
                .andExpression("userId").`as`("userId")
                .andExpression("lastSeen").`as`("lastSeen")
                .andExpression("joinedAt").`as`("joinedAt")
                .and("user.username").`as`("username")
                .and("user.profilePictureUrl").`as`("avatar")
        )

        val result = mongoTemplate.aggregate(aggregation, "room_users", RoomUserDto::class.java)
        val deleted = roomUserRepository.deleteByRoomIdAndUserId(
            ObjectId(roomId),
            ObjectId(userId)
        )
        if (deleted == 0L) {
            throw ChatNotFoundException("User not found in room")
        }
        return result.uniqueMappedResult ?: throw ChatNotFoundException("Failed to load user after deleting from room")
    }

    @Transactional
    fun addUserToRoom(roomId: String, userId: String): RoomUserDto {
        if (isUserInRoom(roomId, userId)) {
            throw ChatAlreadyExistsException("User already in room")
        }

        val roomUser = roomUserRepository.save(
            RoomUser(
                roomId = ObjectId(roomId),
                userId = ObjectId(userId)
            )
        )

        val aggregation = newAggregation(
            match(Criteria.where("_id").`is`(roomUser.id)),
            lookup(
                "User",
                "userId",
                "_id",
                "user"
            ),
            unwind("user"),
            project()
                .andExpression("_id").`as`("id")
                .andExpression("roomId").`as`("roomId")
                .andExpression("userId").`as`("userId")
                .andExpression("lastSeen").`as`("lastSeen")
                .andExpression("joinedAt").`as`("joinedAt")
                .and("user.username").`as`("username")
                .and("user.profilePictureUrl").`as`("avatar")
        )

        val result = mongoTemplate.aggregate(aggregation, "room_users", RoomUserDto::class.java)
        return result.uniqueMappedResult ?: throw ChatNotFoundException("Failed to load user after adding to room")
    }
}