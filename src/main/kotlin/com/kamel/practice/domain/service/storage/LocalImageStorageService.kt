package com.kamel.practice.domain.service.storage

import com.kamel.practice.domain.exception.ChatException
import com.kamel.practice.domain.exception.ChatNotFoundException
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.time.LocalDate
import java.util.*

@Service
class LocalImageStorageService(properties: ImageStorageProperties) {
    private val rootPath: Path = Paths.get(properties.basePath)

    @Throws(IOException::class)
    fun storeFile(inputStream: InputStream, originalName: String): String {
        val today = LocalDate.now()
        val dateDirectory = rootPath.resolve(
            today.year.toString() + File.separator + String.format(
                "%02d",
                today.monthValue
            ) + File.separator + String.format("%02d", today.dayOfMonth)
        )

        Files.createDirectories(dateDirectory)

        val ext = getFileExtension(originalName)
        val storedName = UUID.randomUUID().toString() + (if (ext.isEmpty()) "" else ".$ext")
        val filePath = dateDirectory.resolve(storedName)

        Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW).use { outputStream ->
            StreamUtils.copy(inputStream, outputStream)
        }
        return rootPath.relativize(filePath).toString()
    }

    @Throws(IOException::class)
    fun deleteFile(storedPath: String) {
        val filePath = rootPath.resolve(storedPath).normalize().toAbsolutePath()
        val normalizedRoot = rootPath.normalize().toAbsolutePath()

        if (!filePath.startsWith(normalizedRoot)) {
            throw ChatException("Access denied")
        }

        if (!Files.exists(filePath)) {
            throw ChatNotFoundException("File not found")
        }

        Files.delete(filePath)
    }

    @Throws(IOException::class)
    fun getFileResource(storedPath: String): Resource {
        val filePath = rootPath.resolve(storedPath).normalize().toAbsolutePath()
        val normalizedRoot = rootPath.normalize().toAbsolutePath()

        if (!filePath.startsWith(normalizedRoot)) {
            throw ChatException("Access denied")
        }

        if (!Files.exists(filePath)) {
            throw ChatNotFoundException("File not found")
        }

        return UrlResource(filePath.toUri())
    }

    private fun getFileExtension(fileName: String): String {
        val lastDot = fileName.lastIndexOf('.')
        return if (lastDot == -1) "" else fileName.substring(lastDot + 1)
    }
}