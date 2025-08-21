package com.kamel.practice.domain.service.message

import com.kamel.practice.api.dto.message.ChatMessageResponseDto
import com.kamel.practice.api.dto.message.toDto
import com.kamel.practice.data.model.ChatMessage
import com.kamel.practice.data.repository.ChatMessageRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.*
import org.springframework.data.mongodb.core.aggregation.LookupOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

@Service
class ChatMessageService(
    private val chatMessageRepository: ChatMessageRepository,
    private val mongoTemplate: MongoTemplate,
) {
    fun findMessagesByRoomId(roomId: String): List<ChatMessageResponseDto> {
        val lookup = LookupOperation.newLookup()
            .from("User")
            .localField("senderId")
            .foreignField("_id")
            .`as`("user")

        val aggregation = newAggregation(
            match(Criteria.where("roomId").`is`(ObjectId(roomId))),
            lookup,
            unwind("user", true),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"),
            project("roomId", "senderId", "content", "timestamp", "messageType")
                .and("_id").`as`("id")
                .and("user.username").`as`("senderUsername")
                .and("user.profilePictureUrl").`as`("senderProfilePictureUrl")
        )

        return mongoTemplate.aggregate(
            aggregation,
            "messages",
            ChatMessageResponseDto::class.java
        ).mappedResults
    }

    fun findMessagesByRoomIdAndSenderId(roomId: String, senderId: String): List<ChatMessageResponseDto> {
        val lookup = LookupOperation.newLookup()
            .from("User")
            .localField("senderId")
            .foreignField("_id")
            .`as`("user")

        val aggregation = newAggregation(
            match(Criteria.where("roomId").`is`(roomId).and("senderId").`is`(senderId)),
            lookup,
            unwind("user", true),
            sort(org.springframework.data.domain.Sort.Direction.DESC, "timestamp"),
            project("roomId", "senderId", "content", "timestamp", "messageType")
                .and("_id").`as`("id")
                .and("user.username").`as`("senderUsername")
                .and("user.profilePictureUrl").`as`("senderProfilePictureUrl")
        )

        return mongoTemplate.aggregate(
            aggregation,
            "messages",
            ChatMessageResponseDto::class.java
        ).mappedResults
    }

    fun saveMessage(roomId: String, senderId: String, content: String, type: ChatMessage.MessageType) =
        chatMessageRepository.save(
            ChatMessage(
                roomId = ObjectId(roomId),
                senderId = ObjectId(senderId),
                content = content,
                messageType = type
            )
        ).toDto()
}