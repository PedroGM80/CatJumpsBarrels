package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository

/**
 * UseCase para verificar si un score califica para el top 40.
 */
class CheckHighScoreUseCase(
    private val repository: HighScoreRepository
) {
    suspend operator fun invoke(score: Int): Boolean {
        return repository.isHighScore(score)
    }
}
