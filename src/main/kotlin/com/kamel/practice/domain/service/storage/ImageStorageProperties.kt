package com.kamel.practice.domain.service.storage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "app.image-storage")
@Component
data class ImageStorageProperties(
    val basePath: String = "./images",
    val allowedMimeTypes: MutableSet<String> = mutableSetOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp",
        "image/gif"
    )
)