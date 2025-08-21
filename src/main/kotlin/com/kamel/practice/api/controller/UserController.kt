package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.ServerResponse
import com.kamel.practice.api.dto.user.UserDto
import com.kamel.practice.api.dto.user.UserLoginDto
import com.kamel.practice.api.dto.user.UserRegisterDto
import com.kamel.practice.data.model.ImageMetadata
import com.kamel.practice.domain.exception.ChatException
import com.kamel.practice.domain.service.storage.ImageService
import com.kamel.practice.domain.service.user.UserService
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val imageService: ImageService,
) {
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @Valid @RequestBody userRegisterDto: UserRegisterDto
    ) = ServerResponse.success(
        data = userService.registerUser(
            userRegisterDto.username,
            userRegisterDto.email,
            userRegisterDto.password
        ),
        successMessage = "Otp sent successfully to ${userRegisterDto.email}",
        code = HttpStatus.CREATED.value()
    )

    @PostMapping("/auth/validate-otp")
    fun validateOtp(
        @RequestParam userId: String,
        @RequestParam otp: String
    ) = ServerResponse.success(
        data = userService.validateOtp(userId, otp),
        successMessage = "OTP validated successfully",
    )

    @PostMapping("/auth/login")
    fun loginUser(
        @Valid @RequestBody userLoginDto: UserLoginDto,
    ) = ServerResponse.success(
        data = userService.loginUser(userLoginDto.email, userLoginDto.password),
        successMessage = "User logged in successfully",
    )

    @GetMapping("/{userId}")
    fun getUserById(
        @PathVariable userId: String
    ) = ServerResponse.success(
        data = userService.getUserById(userId),
        successMessage = "User retrieved successfully",
    )

    @GetMapping
    fun getAllUsers() = ServerResponse.success(
        data = userService.getAllUsers(),
        successMessage = "All users retrieved successfully",
    )

    @PostMapping("/auth/logout")
    fun logoutUser(
        @RequestParam userId: String
    ) = ServerResponse.success(
        data = userService.logoutUser(userId),
        successMessage = "User logged out successfully",
    )

    @DeleteMapping("/{userId}")
    fun deleteUser(
        @PathVariable userId: String
    ): ServerResponse<String> {
        userService.deleteUser(userId)
        return ServerResponse.success(
            data = "User with ID $userId deleted successfully",
            successMessage = "User deleted successfully",
        )
    }

    @DeleteMapping("/image/delete")
    @Transactional
    fun deleteUserImage(
        @RequestParam userId: String
    ): ServerResponse<String> {
        userService.deleteUserPicture(userId)
        imageService.deleteImage(userId, ImageMetadata.ImageType.PROFILE)
        return ServerResponse.success(
            data = "User image deleted successfully",
            successMessage = "User image deleted successfully",
        )
    }

    @GetMapping("/active")
    fun getActiveUsers() = ServerResponse.success(
        data = userService.getActiveUsers(),
        successMessage = "Active users retrieved successfully",
    )

    @PostMapping("/image/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    fun uploadUserImage(
        @RequestParam userId: String,
        @RequestParam file: MultipartFile?
    ): ServerResponse<UserDto> {
        if (file == null || file.isEmpty) {
            throw ChatException("File is empty or not provided.")
        }
        val imageMetaData = file.let {
            imageService.uploadImage(it, userId, ImageMetadata.ImageType.PROFILE)
        }
        return ServerResponse.success(
            data = userService.addPicture(userId, imageMetaData.storedName),
            successMessage = "User image uploaded successfully",
        )
    }

    @PatchMapping("/image/replace", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Transactional
    fun replaceUserImage(
        @RequestParam userId: String,
        @RequestParam file: MultipartFile?
    ): ServerResponse<UserDto> {
        if (file == null || file.isEmpty) {
            throw ChatException("File is empty or not provided.")
        }
        val imageMetaData = file.let {
            imageService.replaceImage(it, userId, ImageMetadata.ImageType.PROFILE)
        }
        return ServerResponse.success(
            data = userService.addPicture(userId, imageMetaData.storedName),
            successMessage = "User image replaced successfully",
        )
    }

    @GetMapping("/image/download")
    fun downloadUserImage(
        @RequestParam userId: String,
        response: HttpServletResponse,
    ): Resource {
        val ownerId = userService.downloadUserPicture(userId)
        val metadata = imageService.getImageMetadata(ownerId, ImageMetadata.ImageType.PROFILE)
        val resource = imageService.getImageResource(ownerId, ImageMetadata.ImageType.PROFILE)
        response.contentType = metadata.mimeType
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.originalName + "\"")
        response.setContentLengthLong(metadata.size)
        return resource
    }
}