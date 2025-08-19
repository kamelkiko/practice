package com.kamel.practice.domain.service.car

import com.kamel.practice.api.dto.BrandCountDto
import com.kamel.practice.data.model.Car
import com.kamel.practice.data.repository.CarRepository
import com.kamel.practice.domain.exception.CarNotFoundException
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationResults
import org.springframework.stereotype.Service

@Service
class CarService(
    private val carRepository: CarRepository,
    private val mongoTemplate: MongoTemplate,
) {
    fun getAllCars() = carRepository.findAll()

    fun getCarByCode(code: String) = carRepository.findByCode(code)

    fun getCarById(id: String) = carRepository.findById(ObjectId(id))

    fun getCarByBrand(brand: String) = carRepository.findByBrand(brand)

    fun getCarByModel(model: String) = carRepository.findByModel(model)

    fun getCarByYear(year: Int) = carRepository.findByYear(year)

    fun getCarByPrice(price: Double) = carRepository.findByPrice(price)

    fun getCarByColor(color: String) = carRepository.findByColor(color)

    fun getCarByBrandAndModel(brand: String, model: String) = carRepository.findByBrandAndModel(brand, model)

    fun getCarsByPriceRange(minPrice: Double, maxPrice: Double) =
        carRepository.findByPriceBetween(minPrice, maxPrice)

    fun getCarsByYearRange(minYear: Int, maxYear: Int) = carRepository.findByYearBetween(minYear, maxYear)

    fun saveCar(car: Car) = carRepository.save(car)

    fun updateCar(id: String, car: Car): Car {
        val existingCar = getCarById(id).orElseThrow { CarNotFoundException("Car with id $id not found") }
        val updatedCar = existingCar.copy(
            code = car.code,
            brand = car.brand,
            model = car.model,
            year = car.year,
            price = car.price,
            color = car.color ?: existingCar.color,
            pictureUrl = car.pictureUrl ?: existingCar.pictureUrl
        )
        return carRepository.save(updatedCar)
    }

    fun deleteCarById(id: String) = carRepository.deleteById(ObjectId(id))

    fun getCarCountByBrand(): List<BrandCountDto> {
        val aggregation = Aggregation.newAggregation(
            Aggregation.group("brand").count().`as`("total"),
            Aggregation.project("total").and("brand").previousOperation()
        )

        val results: AggregationResults<BrandCountDto> =
            mongoTemplate.aggregate(aggregation, "cars", BrandCountDto::class.java)

        return results.mappedResults
    }
}