package dev.pgm.game.data.repository

import dev.pgm.game.domain.repository.HighScoreRepository
import dev.pgm.game.model.dto.HighScoreDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class HighScoreRepositoryImpl : HighScoreRepository {

    private val storageDir = File(System.getProperty("user.home"), ".catjumpbarrels")
    private val highScoresFile = File(storageDir, "highscores.json")

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    override suspend fun getHighScores(): List<HighScoreDto> {
        if (!highScoresFile.exists()) {
            return emptyList()
        }
        return try {
            val content = highScoresFile.readText()
            json.decodeFromString<List<HighScoreDto>>(content)
        } catch (e: Exception) {
            // En caso de archivo corrupto, devolver lista vacía
            emptyList()
        }
    }

    override suspend fun saveHighScores(scores: List<HighScoreDto>) {
        val sortedScores = scores.sortedByDescending { it.score }.take(40)
        val content = json.encodeToString(sortedScores)
        highScoresFile.writeText(content)
    }
}
