package com.kamel.practice.data.repository

import com.kamel.practice.data.model.Car
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : MongoRepository<Car, ObjectId> {
    fun findByCode(code: String): Car?

    fun findByBrand(brand: String): List<Car>

    fun findByModel(model: String): List<Car>

    fun findByYear(year: Int): List<Car>

    fun findByBrandAndModel(brand: String, model: String): List<Car>

    fun findByPrice(price: Double): List<Car>

    fun findByPriceBetween(minPrice: Double, maxPrice: Double): List<Car>

    fun findByYearBetween(minYear: Int, maxYear: Int): List<Car>

    fun findByColor(color: String): List<Car>
}