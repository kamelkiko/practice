package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.*
import com.kamel.practice.domain.exception.CarNotFoundException
import com.kamel.practice.domain.service.CarService
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cars")
class CarController(
    private val carService: CarService,
    @Value("\${spring.application.version}")
    private val version: String,
) {
    @PostConstruct
    fun printVersion() {
        println(version)
    }

    @GetMapping
    fun getAllCars(): ServerResponse<List<CarDto>> {
        val cars = carService.getAllCars()
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/code/{code}")
    fun getCarByCode(@PathVariable code: String): ServerResponse<CarDto> {
        val car = carService.getCarByCode(code)
            ?: throw CarNotFoundException("Car with code $code not found.")
        return sendSuccessResponse(
            data = car.toDto(),
            successMessage = "Car retrieved successfully."
        )
    }

    @GetMapping("/{id}")
    fun getCarById(@PathVariable id: String): ServerResponse<CarDto> {
        val car = carService.getCarById(id)
            .orElseThrow { CarNotFoundException("Car with id $id not found.") }
        return sendSuccessResponse(
            data = car.toDto(),
            successMessage = "Car retrieved successfully."
        )
    }

    @GetMapping("/brand/{brand}")
    fun getCarByBrand(@PathVariable brand: String): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByBrand(brand)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/model/{model}")
    fun getCarByModel(@PathVariable model: String): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByModel(model)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/year/{year}")
    fun getCarByYear(@PathVariable year: Int): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByYear(year)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/price/{price}")
    fun getCarByPrice(@PathVariable price: Double): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByPrice(price)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/color/{color}")
    fun getCarByColor(@PathVariable color: String): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByColor(color)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/brand/{brand}/model/{model}")
    fun getCarByBrandAndModel(
        @PathVariable brand: String,
        @PathVariable model: String
    ): ServerResponse<List<CarDto>> {
        val cars = carService.getCarByBrandAndModel(brand, model)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/price-range")
    fun getCarsByPriceRange(
        @RequestParam("minPrice", required = true, defaultValue = "0.0") minPrice: Double,
        @RequestParam("maxPrice", required = true, defaultValue = "0.0") maxPrice: Double
    ): ServerResponse<List<CarDto>> {
        val cars = carService.getCarsByPriceRange(minPrice, maxPrice)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @GetMapping("/year-range")
    fun getCarsByYearRange(
        @RequestParam("minYear", required = true, defaultValue = "0") minYear: Int,
        @RequestParam("maxYear", required = true, defaultValue = "0") maxYear: Int
    ): ServerResponse<List<CarDto>> {
        val cars = carService.getCarsByYearRange(minYear, maxYear)
        return sendSuccessResponse(
            data = cars.map { it.toDto() },
            successMessage = "Cars retrieved successfully."
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun saveCar(@Valid @RequestBody carDto: CarDto): ServerResponse<CarDto> {
        val car = carService.saveCar(carDto.toEntity())
        return sendSuccessResponse(
            data = car.toDto(),
            successMessage = "Car saved successfully.",
            code = HttpStatus.CREATED.value()
        )
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateCar(
        @PathVariable id: String,
        @Valid @RequestBody carDto: CarDto
    ): ServerResponse<CarDto> {
        val updatedCar = carService.updateCar(id, carDto.toEntity())
        return sendSuccessResponse(
            data = updatedCar.toDto(),
            successMessage = "Car updated successfully.",
            code = HttpStatus.ACCEPTED.value()
        )
    }

    @DeleteMapping("/{id}")
    fun deleteCarById(@PathVariable id: String): ServerResponse<Unit> {
        carService.deleteCarById(id)
        return sendSuccessResponse(
            data = Unit,
            successMessage = "Car deleted successfully."
        )
    }
}