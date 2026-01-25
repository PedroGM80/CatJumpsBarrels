package dev.pgm.game.presentation.viewmodel

import dev.pgm.game.domain.usecase.GetTop40ScoresUseCase
import dev.pgm.game.model.entities.HighScore
import dev.pgm.game.presentation.mapper.toHighScore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de High Scores.
 * Gestiona la carga y visualización de los top 40 mejores puntajes.
 */
class HighScoresViewModel(
    private val getTop40ScoresUseCase: GetTop40ScoresUseCase
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<HighScoresUiState>(HighScoresUiState.Loading)
    val uiState: StateFlow<HighScoresUiState> = _uiState.asStateFlow()

    init {
        loadHighScores()
    }

    /**
     * Carga los high scores desde el repositorio.
     */
    fun loadHighScores() {
        coroutineScope.launch {
            _uiState.value = HighScoresUiState.Loading
            try {
                val scores = getTop40ScoresUseCase().map { it.toHighScore() }
                _uiState.value = HighScoresUiState.Success(scores)
            } catch (e: Exception) {
                _uiState.value = HighScoresUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * Estados posibles de la UI de High Scores.
 */
sealed class HighScoresUiState {
    data object Loading : HighScoresUiState()
    data class Success(val scores: List<HighScore>) : HighScoresUiState()
    data class Error(val message: String) : HighScoresUiState()
}
