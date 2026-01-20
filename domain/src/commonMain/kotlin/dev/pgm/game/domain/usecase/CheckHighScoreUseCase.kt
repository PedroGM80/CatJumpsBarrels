package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository

/**
 * Caso de uso para verificar si un score es un high score.
 */
class CheckHighScoreUseCase(
    private val highScoreRepository: HighScoreRepository
) {
    suspend operator fun invoke(score: Int): Boolean {
        val highScores = highScoreRepository.getHighScores()
        if (highScores.size < 40) {
            return true
        }
        val minScore = highScores.minByOrNull { it.score }?.score ?: 0
        return score > minScore
    }
}
