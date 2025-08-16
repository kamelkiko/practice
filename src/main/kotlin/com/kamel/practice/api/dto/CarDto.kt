package com.kamel.practice.api.dto

import com.kamel.practice.data.model.Car
import java.util.*

data class CarDto(
    val code: String? = null,
    val brand: String,
    val model: String,
    val year: Int,
    val price: Double,
    val color: String? = null,
    val pictureUrl: String? = null,
)

fun CarDto.toCar(): Car {
    return Car(
        code = code ?: createRandomCode(model),
        brand = brand,
        model = model,
        year = year,
        price = price,
        color = color,
        pictureUrl = pictureUrl
    )
}

private fun createRandomCode(prefix: String): String {
    return prefix + "-" + UUID.randomUUID().toString().split("-").first()
}