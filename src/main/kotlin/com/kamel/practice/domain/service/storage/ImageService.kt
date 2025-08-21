package com.kamel.practice.domain.service.storage

import com.kamel.practice.data.model.ImageMetadata
import com.kamel.practice.data.repository.ImageMetadataRepository
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
    fun uploadImage(file: MultipartFile, ownerId: String, type: ImageMetadata.ImageType): ImageMetadata {
        validateImage(file)

        val storagePath: String?
        file.inputStream.use { inputStream ->
            storagePath = storageService.storeFile(inputStream, file.originalFilename!!)
        }
        val metadata = ImageMetadata(
            originalName = file.originalFilename!!,
            storedName = storagePath!!,
            mimeType = file.contentType!!,
            ownerId = ownerId,
            size = file.size,
            createdAt = Instant.now(),
            type = type
        )

        return repository.save(metadata)
    }

    fun replaceImage(file: MultipartFile, ownerId: String, type: ImageMetadata.ImageType): ImageMetadata {
        validateImage(file)
        val existingMetadata = repository.findByOwnerIdAndType(ownerId, type)
        existingMetadata?.let {
            storageService.deleteFile(it.storedName)
            repository.deleteById(it.id)
        }
        val storagePath: String?
        file.inputStream.use { inputStream ->
            storagePath = storageService.storeFile(inputStream, file.originalFilename!!)
        }
        val metadata = ImageMetadata(
            originalName = file.originalFilename!!,
            storedName = storagePath!!,
            mimeType = file.contentType!!,
            ownerId = ownerId,
            size = file.size,
            createdAt = Instant.now(),
            type = type
        )

        return repository.save(metadata)
    }

    fun deleteImage(ownerId: String, type: ImageMetadata.ImageType) {
        val metadata = repository.findByOwnerIdAndType(ownerId, type)
            ?: throw FileNotFoundException("File not found.")
        storageService.deleteFile(metadata.storedName)
        repository.deleteById(metadata.id)
    }

    @Throws(IOException::class)
    fun getImageResource(ownerId: String, type: ImageMetadata.ImageType): Resource {
        val metadata: ImageMetadata = getImageMetadata(ownerId, type)
        return storageService.getFileResource(metadata.storedName)
    }

    @Throws(IOException::class)
    fun getImageMetadata(ownerId: String, type: ImageMetadata.ImageType): ImageMetadata {
        return repository.findByOwnerIdAndType(ownerId, type) ?: throw FileNotFoundException("File not found.")
    }

    private fun validateImage(file: MultipartFile) {
        require(!file.isEmpty) { "File is empty." }

        val mimeType = file.contentType
        require(!(mimeType == null || !properties.allowedMimeTypes.contains(mimeType))) { "Invalid mime type." }
    }
}