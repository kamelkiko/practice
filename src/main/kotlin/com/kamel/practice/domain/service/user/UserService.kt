package com.kamel.practice.domain.service.user

import com.kamel.practice.api.dto.user.UserDto
import com.kamel.practice.api.dto.user.UserUpdateDto
import com.kamel.practice.api.dto.user.toDto
import com.kamel.practice.data.model.User
import com.kamel.practice.data.repository.UserRepository
import com.kamel.practice.domain.exception.ChatAlreadyExistsException
import com.kamel.practice.domain.exception.ChatNotFoundException
import com.kamel.practice.domain.service.email.EmailService
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository,
    private val emailService: EmailService,
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

        val otp = generateOtp()
        emailService.sendEmail(email, "Your OTP Code", "Your OTP code is: $otp")

        val newUser = User(
            code = UUID.randomUUID().toString(),
            username = username,
            email = email,
            password = password,
            status = User.Status.OFFLINE,
            otp = otp
        )
        return userRepository.save(newUser).toDto()
    }

    fun validateOtp(
        userId: String,
        otp: String
    ): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            throw ChatNotFoundException("User not found with ID: $userId")
        }
        if (user.otp == null || user.otp.isEmpty()) {
            throw ChatNotFoundException("No OTP found for user with ID: $userId")
        }
        if (user.otp != otp) {
            throw ChatNotFoundException("Invalid OTP for email: ${user.email}")
        }
        // Clear OTP after successful validation
        val updatedUser = user.copy(otp = null)
        return userRepository.save(updatedUser).toDto()
    }

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    fun loginUser(
        email: String,
        password: String,
    ): UserDto {
        val user = userRepository.findByEmail(email)
        return if (user != null && user.password == password) {
            if (user.otp != null && user.otp.isNotEmpty()) {
                throw ChatNotFoundException("User needs to validate OTP before logging in")
            }
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

    fun getUserById(userId: String): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        return user.toDto()
    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { it.toDto() }
    }

    fun addPicture(
        userId: String,
        pictureUrl: String
    ): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        val updatedUser = user.copy(profilePictureUrl = pictureUrl)
        return userRepository.save(updatedUser).toDto()
    }

    fun deleteUser(userId: String): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        userRepository.delete(user)
        return user.toDto()
    }

    fun downloadUserPicture(
        userId: String
    ): String {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        if (user.profilePictureUrl == null || user.profilePictureUrl.isEmpty()) {
            throw ChatNotFoundException("User does not have a profile picture")
        }
        return user.id.toHexString()
    }

    fun deleteUserPicture(
        userId: String
    ): UserDto {
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        if (user.profilePictureUrl == null || user.profilePictureUrl.isEmpty()) {
            throw ChatNotFoundException("User does not have a profile picture to delete")
        }
        val updatedUser = user.copy(profilePictureUrl = null)
        return userRepository.save(updatedUser).toDto()
    }

    fun updateUser(userId: String, user: UserUpdateDto): UserDto {
        val existingUser = userRepository.findById(ObjectId(userId)).orElseThrow {
            ChatNotFoundException("User not found with ID: $userId")
        }
        if (user.username != null && userRepository.existsByUsername(user.username) && user.username != existingUser.username) {
            throw ChatAlreadyExistsException("Username already exists - ${user.username}")
        }
        if (user.email != null && userRepository.existsByEmail(user.email) && user.email != existingUser.email) {
            throw ChatAlreadyExistsException("Email already exists - ${user.email}")
        }
        val updatedUser = existingUser.copy(
            username = user.username ?: existingUser.username,
            email = user.email ?: existingUser.email,
            password = user.password ?: existingUser.password
        )
        return userRepository.save(updatedUser).toDto()
    }
}