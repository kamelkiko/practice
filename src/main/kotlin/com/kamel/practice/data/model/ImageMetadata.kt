package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("image_metadata")
data class ImageMetadata(
    @Id
    val id: ObjectId = ObjectId.get(),
    val originalName: String,
    val storedName: String,
    val mimeType: String,
    val ownerId: String,
    val size: Long,
    val type: ImageType,
    val createdAt: Instant = Instant.now(),
) {
    enum class ImageType {
        PROFILE,
        ROOM,
    }
}