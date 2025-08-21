package com.kamel.practice.data.repository

import com.kamel.practice.data.model.ImageMetadata
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageMetadataRepository : MongoRepository<ImageMetadata, ObjectId> {
    fun findByOwnerIdAndType(ownerId: String, type: ImageMetadata.ImageType): ImageMetadata?
}