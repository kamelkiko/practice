package com.kamel.practice.controller

import com.kamel.practice.repo.ServerResponse
import com.kamel.practice.service.BrandCount
import com.kamel.practice.service.Car
import com.kamel.practice.service.CarService
import com.kamel.practice.util.GameNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/cars")
class CarController(
    private val carService: CarService,
) {
    @GetMapping
    fun getAllCars(): ServerResponse<List<Car>> {
        return ServerResponse.success(
            data = carService.getAllCars(),
            successMessage = "All Cars retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/brand/{brand}")
    fun getCarsByBrand(@PathVariable brand: String): ServerResponse<List<Car>> {
        val cars = carService.getCarsByBrand(brand)
        if (cars.isEmpty()) {
            throw GameNotFoundException("No cars found for brand $brand")
        }
        return ServerResponse.success(
            data = cars,
            successMessage = "Cars with brand $brand retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/model/{model}")
    fun getCarsByModel(@PathVariable model: String): ServerResponse<List<Car>> {
        val cars = carService.getCarsByModel(model)
        if (cars.isEmpty()) {
            throw GameNotFoundException("No cars found for model $model")
        }
        return ServerResponse.success(
            data = cars,
            successMessage = "Cars with model $model retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/year/{year}")
    fun getCarsByYear(@PathVariable year: Int): ServerResponse<List<Car>> {
        val cars = carService.getCarsByYear(year)
        if (cars.isEmpty()) {
            throw GameNotFoundException("No cars found for year $year")
        }
        return ServerResponse.success(
            data = cars,
            successMessage = "Cars from year $year retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCar(@Valid @RequestBody car: Car): ServerResponse<Car> {
        val createdCar = carService.addCar(car)
        return ServerResponse.success(
            data = createdCar,
            successMessage = "Car with ID ${createdCar.id} created successfully",
            code = HttpStatus.CREATED.value()
        )
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateCarById(@PathVariable id: String, @Valid @RequestBody car: Car): ServerResponse<Car> {
        val updatedCar = carService.updateCar(id, car)
        return ServerResponse.success(
            data = updatedCar,
            successMessage = "Car with ID ${updatedCar.id} updated successfully",
            code = HttpStatus.ACCEPTED.value()
        )
    }

    @DeleteMapping("/{id}")
    fun deleteCarById(@PathVariable id: String): ServerResponse<Boolean> {
        carService.deleteCar(id)
        return ServerResponse.success(
            data = true,
            successMessage = "Car with ID $id deleted successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/{id}")
    fun getCarById(@PathVariable id: String): ServerResponse<Car> {
        val car = carService.getCarById(id) ?: throw GameNotFoundException("Car with id $id not found")
        return ServerResponse.success(
            data = car,
            successMessage = "Car with ID $id retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/brand-count")
    fun getCarCountByBrand(): ServerResponse<List<BrandCount>> {
        val brandCounts = carService.getCarCountByBrand()
        return ServerResponse.success(
            data = brandCounts,
            successMessage = "Car count by brand retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }
}