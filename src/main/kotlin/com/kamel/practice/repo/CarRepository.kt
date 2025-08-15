package com.kamel.practice.repo

import com.kamel.practice.service.Car
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : MongoRepository<Car, ObjectId> {
    fun findByBrand(brand: String): List<Car>
    fun findByModel(model: String): List<Car>
    fun findByYear(year: Int): List<Car>
}