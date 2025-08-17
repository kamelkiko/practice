package com.kamel.practice.domain.service

import com.kamel.practice.data.model.Car
import com.kamel.practice.data.repository.CarRepository
import com.kamel.practice.domain.exception.CarNotFoundException
import org.bson.types.ObjectId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CarService(
    private val carRepository: CarRepository
) {
    @Cacheable(value = ["cars_all"])
    fun getAllCars() = carRepository.findAll()

    fun getCarByCode(code: String) = carRepository.findByCode(code)

    @Cacheable(value = ["cars"], key = "#id")
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

    @CachePut(value = ["cars"], key = "#id")
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

    @CacheEvict(value = ["cars"], key = "#id")
    fun deleteCarById(id: String) = carRepository.deleteById(ObjectId(id))
}