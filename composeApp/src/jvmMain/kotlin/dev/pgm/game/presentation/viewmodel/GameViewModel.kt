package dev.pgm.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dev.pgm.game.domain.usecase.*
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel principal del juego.
 * Responsabilidad: Orquestar los UseCases y gestionar el estado del juego.
 * Sigue el patrón de Clean Architecture inspirado en Antonio Leiva.
 */
class GameViewModel(
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val updateBarrelsUseCase: UpdateBarrelsUseCase,
    private val checkCollisionsUseCase: CheckCollisionsUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(
        GameState.initial(androidx.compose.ui.unit.IntSize(800, 700))
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Actualiza el estado del juego cada frame.
     * Orquesta todos los UseCases en el orden correcto.
     */
    fun tick(deltaTime: Float, input: GameInput) {
        val currentState = _gameState.value

        if (currentState.isGameOver || currentState.isWon || currentState.isPaused) {
            return
        }

        var newState = currentState

        // 1. Actualizar jugador
        if (newState.player.state != dev.pgm.game.model.entities.PlayerState.DEAD) {
            newState = updatePlayerUseCase(newState, input, deltaTime)
        }

        // 2. Actualizar barriles
        newState = updateBarrelsUseCase(newState)

        // 3. Verificar colisiones
        newState = checkCollisionsUseCase(newState)

        // 4. Actualizar condición de victoria
        newState = checkWinCondition(newState)

        _gameState.value = newState
    }

    fun togglePause() {
        _gameState.value = _gameState.value.copy(isPaused = !_gameState.value.isPaused)
    }

    fun restart() {
        _gameState.value = GameState.initial(
            androidx.compose.ui.unit.IntSize(800, 700)
        )
    }

    fun setScreenSize(width: Int, height: Int) {
        _gameState.value = _gameState.value.copy(
            screenSize = androidx.compose.ui.unit.IntSize(width, height)
        )
    }

    private fun checkWinCondition(state: GameState): GameState {
        val playerCenterX = state.player.position.x + state.player.size / 2
        val playerCenterY = state.player.position.y + state.player.size / 2

        val winZone = dev.pgm.game.model.utils.GameRect(
            state.winObjetive.position.x,
            state.winObjetive.position.y,
            state.winObjetive.size,
            state.winObjetive.size
        )

        val playerInWinZone = playerCenterX >= winZone.x &&
                              playerCenterX <= winZone.x + winZone.width &&
                              playerCenterY >= winZone.y &&
                              playerCenterY <= winZone.y + winZone.height

        return if (playerInWinZone && !state.isWon) {
            state.copy(
                isWon = true,
                score = state.score + dev.pgm.game.model.core.GameConstants.POINTS_WIN
            )
        } else {
            state
        }
    }
}
