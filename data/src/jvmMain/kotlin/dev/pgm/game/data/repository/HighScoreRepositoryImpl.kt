package dev.pgm.game.data.repository

import dev.pgm.game.data.database.dao.HighScoreDao
import dev.pgm.game.data.database.entity.HighScoreEntity
import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.entities.HighScore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementación del repositorio de high scores usando Room.
 */
class HighScoreRepositoryImpl(
    private val dao: HighScoreDao
) : HighScoreRepository {

    override suspend fun getTop40Scores(): List<HighScore> {
        return dao.getTop40Scores().map { it.toDomain() }
    }

    override suspend fun isHighScore(score: Int): Boolean {
        val count = dao.getRecordCount()

        // Si hay menos de 40 records, siempre califica
        if (count < 40) return true

        // Si hay 40 o más, verificar si el score es mayor que el mínimo
        val minScore = dao.getMinScore() ?: return true
        return score > minScore
    }

    override suspend fun saveScore(
        playerName: String,
        score: Int,
        timestamp: Long
    ): Result<Unit> {
        return try {
            val entity = HighScoreEntity(
                playerName = playerName,
                score = score,
                timestamp = timestamp
            )

            dao.insertScore(entity)

            // Si hay más de 40 records, eliminar el más bajo
            val count = dao.getRecordCount()
            if (count > 40) {
                dao.deleteLowestScore()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convierte una entidad de Room a modelo de dominio.
     */
    private fun HighScoreEntity.toDomain(): HighScore {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return HighScore(
            id = id,
            playerName = playerName,
            score = score,
            timestamp = timestamp,
            formattedDate = dateFormat.format(Date(timestamp))
        )
    }
}
