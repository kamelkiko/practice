package com.kamel.practice.domain.service.storage

import com.kamel.practice.data.model.ImageMetadata
import com.kamel.practice.data.repository.ImageMetadataRepository
import org.bson.types.ObjectId
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.io.IOException
import java.time.Instant

@Service
class ImageService(
    private val properties: ImageStorageProperties,
    private val repository: ImageMetadataRepository,
    private val storageService: LocalImageStorageService
) {

    @Throws(IOException::class)
    fun uploadImage(file: MultipartFile, ownerId: String?): ImageMetadata {
        validateImage(file)

        val storagePath: String?
        file.inputStream.use { inputStream ->
            storagePath = storageService.storeFile(inputStream, file.originalFilename!!)
        }
        val metadata: ImageMetadata = ImageMetadata(
            originalName = file.originalFilename!!,
            storedName = storagePath!!,
            mimeType = file.contentType!!,
            cardId = ownerId!!,
            size = file.size,
            createdAt = Instant.now(),
        )

        return repository.save(metadata)
    }

    @Throws(IOException::class)
    fun getImageResource(imageId: String): Resource {
        val metadata: ImageMetadata = getImageMetadata(imageId)
        return storageService.getFileResource(metadata.storedName)
    }

    @Throws(IOException::class)
    fun getImageMetadata(imageId: String): ImageMetadata {
        val objectId = ObjectId(imageId)
        return repository.findById(objectId).orElseThrow(
            { FileNotFoundException("File not found.") }
        )
    }

    private fun validateImage(file: MultipartFile) {
        require(!file.isEmpty) { "File is empty." }

        val mimeType = file.contentType
        require(!(mimeType == null || !properties.allowedMimeTypes.contains(mimeType))) { "Invalid mime type." }
    }
}