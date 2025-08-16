package com.kamel.practice.data.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("cars")
data class Car(
    @Id
    val id: ObjectId = ObjectId.get(),
    val code: String,
    val brand: String,
    val model: String,
    val year: Int,
    val price: Double,
    val color: String? = null,
    val pictureUrl: String? = null,
)