package com.kamel.practice.service

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("cars")
data class Car(
    @Id
    val id: ObjectId = ObjectId.get(),
    val name: String,
    val brand: String,
    val model: String,
    val year: Int,
    val price: Double
)