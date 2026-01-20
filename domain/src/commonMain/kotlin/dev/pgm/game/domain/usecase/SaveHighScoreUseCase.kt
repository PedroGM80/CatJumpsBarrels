package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository

/**
 * UseCase para guardar un nuevo high score.
 */
class SaveHighScoreUseCase(
    private val repository: HighScoreRepository
) {
    suspend operator fun invoke(playerName: String, score: Int): Result<Unit> {
        val timestamp = System.currentTimeMillis()
        return repository.saveScore(playerName, score, timestamp)
    }
}
