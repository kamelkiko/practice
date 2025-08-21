package com.kamel.practice.domain.service

import com.kamel.practice.api.dto.UserDto
import com.kamel.practice.api.dto.toDto
import com.kamel.practice.data.model.User
import com.kamel.practice.data.repository.UserRepository
import com.kamel.practice.domain.exception.ChatAlreadyExistsException
import com.kamel.practice.domain.exception.ChatNotFoundException
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    fun registerUser(
        username: String,
        email: String,
        password: String,
    ): UserDto {
        if (userRepository.existsByUsername(username)) {
            throw ChatAlreadyExistsException("Username already exists - $username")
        }
        if (userRepository.existsByEmail(email)) {
            throw ChatAlreadyExistsException("Email already exists - $email")
        }
        val newUser = User(
            code = UUID.randomUUID().toString(),
            username = username,
            email = email,
            password = password,
            status = User.Status.OFFLINE
        )
        return userRepository.save(newUser).toDto()
    }

    fun loginUser(
        email: String,
        password: String,
    ): UserDto {
        val user = userRepository.findByEmail(email)
        return if (user != null && user.password == password) {
            userRepository.save(user.copy(status = User.Status.ONLINE)).toDto()
        } else throw ChatNotFoundException("Invalid email or password")
    }

    fun logoutUser(userId: String): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        return userRepository.save(user.copy(status = User.Status.OFFLINE)).toDto()
    }

    fun getActiveUsers(): List<UserDto> {
        return userRepository.findAllByStatus(User.Status.ONLINE).map { it.toDto() }
    }
}