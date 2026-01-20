package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.dto.HighScoreDto

/**
 * Caso de uso para guardar un nuevo high score.
 */
class SaveHighScoreUseCase(
    private val highScoreRepository: HighScoreRepository
) {
    suspend operator fun invoke(playerName: String, score: Int) {
        val currentScores = highScoreRepository.getHighScores().toMutableList()
        currentScores.add(
            HighScoreDto(
                playerName = playerName,
                score = score,
                timestamp = System.currentTimeMillis()
            )
        )
        highScoreRepository.saveHighScores(currentScores)
    }
}
