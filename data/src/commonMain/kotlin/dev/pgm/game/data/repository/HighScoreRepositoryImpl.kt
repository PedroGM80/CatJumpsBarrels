package dev.pgm.game.data.repository

import dev.pgm.game.data.storage.DataStorage
import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.dto.HighScoreDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementación multiplataforma del repositorio de high scores.
 * Usa DataStorage para persistencia (archivos en JVM, LocalStorage en Web).
 */
class HighScoreRepositoryImpl : HighScoreRepository {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    companion object {
        private const val STORAGE_KEY = "highscores"
    }
    
    override suspend fun getHighScores(): List<HighScoreDto> {
        val content = DataStorage.read(STORAGE_KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<HighScoreDto>>(content)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun saveHighScores(scores: List<HighScoreDto>) {
        val sortedScores = scores.sortedByDescending { it.score }.take(40)
        val content = json.encodeToString(sortedScores)
        DataStorage.write(STORAGE_KEY, content)
    }
}
