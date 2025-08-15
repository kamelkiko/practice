package com.kamel.practice.service

import com.kamel.practice.repo.GameRepo
import org.springframework.stereotype.Service

@Service
class GameService(
    private val gameRepo: GameRepo
) {
    fun createGame(game: Game) = gameRepo.createGame(game)

    fun getGames() = gameRepo.getGames()

    fun getGameById(id: Long) = gameRepo.getGameById(id)

    fun deleteGameById(id: Long) = gameRepo.deleteGameById(id)

    fun updateGameById(id: Long, game: Game) = gameRepo.updateGameById(id, game)

    fun searchGamesByName(name: String) = gameRepo.searchGamesByName(name)
}