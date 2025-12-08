package dev.pgm.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dev.pgm.game.domain.usecase.*
import dev.pgm.game.input.GameInput
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.CatAnimation
import dev.pgm.game.model.entities.PlayerState
import dev.pgm.game.model.utils.GameRect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel completo del juego con Clean Architecture + Koin.
 * Orquesta todos los UseCases y gestiona el estado del juego.
 */
class GameViewModelComplete(
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val updateBarrelsUseCase: UpdateBarrelsUseCase,
    private val checkCollisionsUseCase: CheckCollisionsUseCase,
    private val spawnBarrelUseCase: SpawnBarrelUseCase,
    private val updateParticlesUseCase: UpdateParticlesUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(
        GameState.initial(androidx.compose.ui.unit.IntSize(800, 700))
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var animationTimer = 0f

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

        // 1. Verificar y respawnear jugador si está muerto
        newState = checkAndRespawnPlayer(newState)

        // 2. Actualizar animación del jugador
        animationTimer += deltaTime
        if (animationTimer > GameConstants.ANIMATION_FRAME_DURATION) {
            animationTimer = 0f
            newState = updatePlayerAnimation(newState, input)
        }

        // 3. Actualizar jugador si no está muerto
        if (newState.player.state != PlayerState.DEAD) {
            newState = updatePlayerUseCase(newState, input, deltaTime)

            // 4. Actualizar barriles
            newState = updateBarrelsUseCase(newState)

            // 5. Spawner barriles
            newState = spawnBarrelUseCase(newState, System.currentTimeMillis())

            // 6. Actualizar partículas
            newState = updateParticlesUseCase(newState, deltaTime)

            // 7. Verificar colisiones
            newState = checkCollisionsUseCase(newState)

            // 8. Actualizar condición de victoria
            newState = checkWinCondition(newState)

            // 9. Limpiar popups antiguos
            newState = cleanupPopups(newState)
        }

        _gameState.value = newState
    }

    fun togglePause() {
        _gameState.value = _gameState.value.copy(isPaused = !_gameState.value.isPaused)
    }

    fun restart() {
        val currentSize = _gameState.value.screenSize
        val currentHighScore = _gameState.value.highScore
        spawnBarrelUseCase.reset()
        animationTimer = 0f
        _gameState.value = GameState.initial(currentSize).copy(highScore = currentHighScore)
    }

    fun setScreenSize(width: Int, height: Int) {
        val size = androidx.compose.ui.unit.IntSize(width, height)
        if (_gameState.value.screenSize != size) {
            val currentHighScore = _gameState.value.highScore
            _gameState.value = GameState.initial(size).copy(highScore = currentHighScore)
        }
    }

    private fun updatePlayerAnimation(state: GameState, input: GameInput): GameState {
        val player = state.player

        // Si está escalando pero no presiona arriba/abajo, pausar la animación
        val isClimbingButStationary = player.state == PlayerState.CLIMBING &&
                !input.up && !input.down

        if (isClimbingButStationary) {
            return state // No actualizar el frame, mantener la animación pausada
        }

        val animation = CatAnimation.animations[player.state]
            ?: CatAnimation.animations[PlayerState.IDLE]!!

        if (animation.isEmpty()) {
            return state // Evitar división por cero
        }

        val nextFrame = (player.animationFrame + 1) % animation.size

        return state.copy(
            player = player.copy(animationFrame = nextFrame)
        )
    }

    private fun checkAndRespawnPlayer(state: GameState): GameState {
        if (state.playerDeathTimestamp == 0L) return state

        val timeSinceDeath = System.currentTimeMillis() - state.playerDeathTimestamp
        if (timeSinceDeath < 1500) return state // 1.5s death animation

        // Respawnear jugador
        val platforms = state.platforms
        if (platforms.isEmpty()) return state

        val startPlatform = platforms[0]
        val playerY = startPlatform.getYAt(50f) - GameConstants.PLAYER_SIZE

        // Reiniciar el flujo de barriles
        spawnBarrelUseCase.reset()

        return state.copy(
            player = state.player.copy(
                position = androidx.compose.ui.geometry.Offset(50f, playerY),
                velocity = androidx.compose.ui.geometry.Offset.Zero,
                state = PlayerState.IDLE,
                isOnGround = true,
                isClimbing = false,
                animationFrame = 0,
                invincibleUntil = System.currentTimeMillis() + GameConstants.INVINCIBILITY_TIME
            ),
            playerDeathTimestamp = 0L
        )
    }

    private fun checkWinCondition(state: GameState): GameState {
        val playerCenterX = state.player.position.x + state.player.size / 2
        val playerCenterY = state.player.position.y + state.player.size / 2

        val winZone = GameRect(
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
            val newScore = state.score + GameConstants.POINTS_WIN
            state.copy(
                isWon = true,
                score = newScore,
                highScore = maxOf(state.highScore, newScore)
            )
        } else {
            state
        }
    }

    private fun cleanupPopups(state: GameState): GameState {
        val popup = state.lastScorePopup ?: return state

        val elapsed = System.currentTimeMillis() - popup.createdAt
        return if (elapsed > GameConstants.SCORE_POPUP_LIFETIME_MS) {
            state.copy(lastScorePopup = null)
        } else {
            state
        }
    }
}
