package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.user.UserLoginDto
import com.kamel.practice.api.dto.user.UserRegisterDto
import com.kamel.practice.domain.service.user.UserService
import com.kamel.practice.api.dto.ServerResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(UserController::class.java)

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun registerUser(
        @RequestBody userRegisterDto: UserRegisterDto
    ) = ServerResponse.success(
        data = userService.registerUser(
            userRegisterDto.username,
            userRegisterDto.email,
            userRegisterDto.password
        ),
        successMessage = "User registered successfully",
        code = HttpStatus.CREATED.value()
    )

    @PostMapping("/auth/login")
    fun loginUser(
        @RequestBody userLoginDto: UserLoginDto,
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

    @GetMapping("/active")
    fun getActiveUsers() = ServerResponse.success(
        data = userService.getActiveUsers(),
        successMessage = "Active users retrieved successfully",
    )
}