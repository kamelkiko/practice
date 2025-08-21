package com.kamel.practice.api.controller

import com.kamel.practice.api.dto.UserLoginDto
import com.kamel.practice.api.dto.UserRegisterDto
import com.kamel.practice.domain.service.UserService
import com.kamel.practice.util.ServerResponse
import io.swagger.v3.oas.annotations.parameters.RequestBody
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService
) {
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
        successMessage = "User - ${userRegisterDto.username} registered successfully",
        code = HttpStatus.CREATED.value()
    )

    @PostMapping("/auth/login")
    fun loginUser(
        @RequestBody userLoginDto: UserLoginDto,
    ) = ServerResponse.success(
        data = userService.loginUser(userLoginDto.email, userLoginDto.password),
        successMessage = "User logged in successfully",
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