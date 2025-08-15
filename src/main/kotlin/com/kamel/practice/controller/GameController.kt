package com.kamel.practice.controller

import com.kamel.practice.repo.EmailRequest
import com.kamel.practice.repo.ServerResponse
import com.kamel.practice.service.EmailSenderService
import com.kamel.practice.service.Game
import com.kamel.practice.service.GameService
import com.kamel.practice.util.GameAlreadyExistsException
import com.kamel.practice.util.GameNotFoundException
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@ConfigurationProperties(prefix = "car")
data class Config(
    val name: String,
)

@RestController
@RequestMapping("/games")
@Profile("dev")
class GameController(
    private val gameService: GameService,
    @param:Value("\${SPRING_PROFILES_ACTIVE}")
    private val version: String,
    private val emailSenderService: EmailSenderService,
    config: Config,
) {
    init {
        println(version)
        println(config)
    }

    @PostMapping("/send-simple-email")
    fun sendSimpleEmail(@RequestBody emailRequest: EmailRequest): ServerResponse<String> {
        emailSenderService.sendEmail(emailRequest.to, emailRequest.subject, emailRequest.body)
        return ServerResponse.success("Email sent successfully!", null)
    }

    @GetMapping
    fun getAllGames(): ServerResponse<List<Game>> {
        return ServerResponse.success(
            data = gameService.getGames(),
            successMessage = "All Games retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/{id}")
    fun getGameById(@PathVariable id: Long): ServerResponse<Game> {
        val game = gameService.getGameById(id) ?: throw GameNotFoundException("Game with id $id not found")
        return ServerResponse.success(
            data = game,
            successMessage = "Game with ID $id retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @GetMapping("/search")
    fun getGamesByName(
        @RequestParam(
            "name",
            required = true,
            defaultValue = ""
        ) name: String
    ): ServerResponse<List<Game>> {
        return ServerResponse.success(
            data = gameService.searchGamesByName(name),
            successMessage = "All Games with query $name retrieved successfully",
            code = HttpStatus.OK.value()
        )
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createGame(@Valid @RequestBody game: Game): ServerResponse<Game> {
        val createdGame =
            gameService.createGame(game) ?: throw GameAlreadyExistsException("Game with id ${game.id} already exists")
        return ServerResponse.success(
            data = createdGame,
            successMessage = "Game with ID ${game.id} created successfully",
            code = HttpStatus.CREATED.value()
        )
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun updateGameById(@PathVariable id: Long, @Valid @RequestBody game: Game): ServerResponse<Game> {
        val updatedGame =
            gameService.updateGameById(id, game) ?: throw GameNotFoundException("Game with id $id not found")
        return ServerResponse.success(
            data = updatedGame,
            successMessage = "Game with ID ${game.id} updated successfully",
            code = HttpStatus.ACCEPTED.value()
        )
    }

    @DeleteMapping("/{id}")
    fun deleteGameById(@PathVariable id: Long): ServerResponse<Boolean> {
        val deleted = gameService.deleteGameById(id)
        if (deleted.not()) {
            throw GameNotFoundException("Game with id $id not found")
        } else {
            return ServerResponse.success(
                data = deleted,
                successMessage = "Game with ID $id deleted successfully",
                code = HttpStatus.OK.value()
            )
        }
    }
}