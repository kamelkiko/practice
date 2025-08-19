package com.kamel.practice.api.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.kamel.practice.api.dto.*
import com.kamel.practice.domain.exception.CarNotFoundException
import com.kamel.practice.domain.exception.FileNotFoundException
import com.kamel.practice.domain.service.car.CarService
import com.kamel.practice.domain.service.storage.ImageService
import jakarta.annotation.PostConstruct
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/cars")
class CarController(
    private val carService: CarService,
    private val imageService: ImageService,
    @Value("\${spring.application.version}")
    private val version: String,
) {
    @PostConstruct
    fun printVersion() {
        println(version)
    }

    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

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
    fun getCarById(
        @PathVariable id: String,
    ): ServerResponse<CarDto> {
        val car = carService.getCarById(id)
            .orElseThrow { CarNotFoundException("Car with id $id not found.") }
//        val contentType = car.pictureUrl?.let {
//            val resource = imageService.loadFile(it)
//            request.servletContext.getMimeType(resource.file.absolutePath)
//                ?: MediaType.APPLICATION_OCTET_STREAM_VALUE
//        }
//        response.contentType = contentType
//        response.addHeader(
//            HttpHeaders.CONTENT_DISPOSITION,
//            "attachment; filename=\"${imageService.loadFile(car.pictureUrl ?: "")}\""
//        )
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

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    fun saveCar(
        @RequestParam("car", required = true) jsonData: String,
        @RequestParam("file", required = false) file: MultipartFile?
    ): ServerResponse<CarDto> {
        val carDto = objectMapper.readValue(jsonData, CarDto::class.java)
        val id = ObjectId.get()
        val imageMetaData = file?.let {
            imageService.uploadImage(it, id.toHexString())
        }
        val car = carService.saveCar(carDto.copy(pictureUrl = imageMetaData?.storedName).toEntity(id))
        return sendSuccessResponse(
            data = car.copy(pictureUrl = imageMetaData?.storedName).toDto(),
            successMessage = "Car saved successfully.",
            code = HttpStatus.CREATED.value()
        )
    }

    @PutMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    fun updateCar(
        @PathVariable id: String,
        @RequestParam("car", required = true) jsonData: String,
        @RequestParam("file", required = false) file: MultipartFile?
    ): ServerResponse<CarDto> {
        val carDto = objectMapper.readValue(jsonData, CarDto::class.java)
            ?: throw CarNotFoundException("Invalid car data provided.")
        val imageMetaData = file?.let {
            imageService.replaceImage(id, it)
        }
        val updatedCar = carService.updateCar(id, carDto.copy(pictureUrl = imageMetaData?.storedName).toEntity())
        return sendSuccessResponse(
            data = updatedCar.toDto(),
            successMessage = "Car updated successfully.",
            code = HttpStatus.ACCEPTED.value()
        )
    }

    @PatchMapping("/{id}/update-image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Transactional
    fun updateCarImage(
        @PathVariable id: String,
        @RequestParam("file", required = false) file: MultipartFile?
    ): ServerResponse<String> {
        val imageMetaData = file?.let {
            imageService.replaceImage(id, it)
        } ?: throw FileNotFoundException("Image file not found for car with ID $id.")
        carService.updateCarImage(id, imageMetaData.storedName)
        return sendSuccessResponse(
            data = imageMetaData.storedName,
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