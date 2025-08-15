package com.kamel.practice.service

import com.kamel.practice.repo.CarRepository
import com.kamel.practice.util.GameNotFoundException
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.stereotype.Service

@Service
class CarService(
    private val carRepository: CarRepository,
    private val mongoTemplate: MongoTemplate,
) {
    fun getAllCars(): List<Car> {
        return carRepository.findAll()
    }

    fun getCarById(id: String): Car? {
        return carRepository.findById(ObjectId(id)).orElse(null)
    }

    fun getCarsByBrand(brand: String): List<Car> {
        return carRepository.findByBrand(brand)
    }

    fun getCarsByModel(model: String): List<Car> {
        return carRepository.findByModel(model)
    }

    fun getCarsByYear(year: Int): List<Car> {
        return carRepository.findByYear(year)
    }

    fun addCar(car: Car): Car {
        return carRepository.save(car)
    }

    fun updateCar(id: String, car: Car): Car {
        if (carRepository.existsById(ObjectId(id)).not()) {
            throw GameNotFoundException("Car with id $id does not exist")
        }
        return carRepository.save(car.copy(id = ObjectId(id)))
    }

    fun deleteCar(id: String) {
        if (carRepository.existsById(ObjectId(id)).not()) {
            throw GameNotFoundException("Car with id $id does not exist")
        }
        carRepository.deleteById(ObjectId(id))
    }

    fun getCarCountByBrand(): List<BrandCount> {
        val aggregation = Aggregation.newAggregation(
            group("brand").count().`as`("total"),
            project("total").and("brand").previousOperation()
        )

        val results: AggregationResults<BrandCount> =
            mongoTemplate.aggregate(aggregation, "cars", BrandCount::class.java)

        return results.mappedResults
    }
}

data class BrandCount(val brand: String, val total: Long)