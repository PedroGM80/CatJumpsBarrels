package dev.pgm.game.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pgm.game.domain.usecase.*
import dev.pgm.game.input.GameInput
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.CatAnimation
import dev.pgm.game.model.entities.BossAnimation
import dev.pgm.game.model.entities.PlayerState
import dev.pgm.game.model.entities.BossState
import dev.pgm.game.core.utils.GameRect

/**
 * Represents a one-time UI event from the ViewModel to the View.
 */
sealed class GameUiEvent {
    data class ShowHighScoreDialog(val score: Int) : GameUiEvent()
}

/**
 * Full game ViewModel with Clean Architecture + Koin.
 * Orchestrates all UseCases and manages game state.
 */
class GameViewModelComplete(
    private val updatePlayerUseCase: UpdatePlayerUseCase,
    private val updateBarrelsUseCase: UpdateBarrelsUseCase,
    private val checkCollisionsUseCase: CheckCollisionsUseCase,
    private val spawnBarrelUseCase: SpawnBarrelUseCase,
    private val updateParticlesUseCase: UpdateParticlesUseCase,
    private val checkHighScoreUseCase: CheckHighScoreUseCase,
    private val saveHighScoreUseCase: SaveHighScoreUseCase
) : ViewModel() {

    private val _gameState = MutableStateFlow(
        GameState.initial(androidx.compose.ui.unit.IntSize(800, 700))
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var animationTimer = 0f

    // One-time UI events
    private val _uiEvent = Channel<GameUiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private var _pendingScore: Int = 0

    /**
     * Updates the game state every frame.
     * Orchestrates all UseCases in the correct order.
     */
    fun tick(deltaTime: Float, input: GameInput) {
        val currentState = _gameState.value

        if (currentState.isGameOver || currentState.isWon || currentState.isPaused) {
            return
        }

        var newState = currentState

        // 1. Check and respawn player if dead
        newState = checkAndRespawnPlayer(newState)

        // 2. Update player and boss animation
        animationTimer += deltaTime
        if (animationTimer > GameConstants.ANIMATION_FRAME_DURATION) {
            animationTimer = 0f
            newState = updatePlayerAnimation(newState, input)
            newState = updateBossAnimation(newState)
        }

        // 3. Update player if not dead
        if (newState.player.state != PlayerState.DEAD) {
            newState = updatePlayerUseCase(newState, input, deltaTime)

            // 4. Update barrels
            newState = updateBarrelsUseCase(newState)

            // 5. Spawn barrels
            newState = spawnBarrelUseCase(newState, System.currentTimeMillis())

            // 6. Update particles
            newState = updateParticlesUseCase(newState, deltaTime)

            // 7. Check collisions
            newState = checkCollisionsUseCase(newState)

            // 8. Update win condition
            newState = checkWinCondition(newState)

            // 9. Clean up old popups
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
        
        // Reset pending score
        _pendingScore = 0
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

        // If climbing but not pressing up/down, pause animation
        val isClimbingButStationary = player.state == PlayerState.CLIMBING &&
                !input.up && !input.down

        if (isClimbingButStationary) {
            return state // Do not update frame, keep animation paused
        }

        val animation = CatAnimation.animations[player.state]
            ?: CatAnimation.animations[PlayerState.IDLE]!!

        if (animation.isEmpty()) {
            return state // Avoid division by zero
        }

        val nextFrame = (player.animationFrame + 1) % animation.size

        return state.copy(
            player = player.copy(animationFrame = nextFrame)
        )
    }

    private fun updateBossAnimation(state: GameState): GameState {
        val boss = state.enemy
        val animation = BossAnimation.animations[boss.state] ?: BossAnimation.animations[BossState.IDLE]!!

        if (animation.isEmpty()) {
            return state
        }

        val nextFrame = (boss.animationFrame + 1) % animation.size

        // If in THROWING state and animation completed, return to IDLE
        val newState = if (boss.state == BossState.THROWING && nextFrame == 0) {
            BossState.IDLE
        } else {
            boss.state
        }

        return state.copy(
            enemy = boss.copy(
                animationFrame = nextFrame,
                state = newState
            )
        )
    }

    private fun checkAndRespawnPlayer(state: GameState): GameState {
        if (state.playerDeathTimestamp == 0L) return state

        val timeSinceDeath = System.currentTimeMillis() - state.playerDeathTimestamp
        if (timeSinceDeath < 1500) return state // 1.5s death animation

        // Respawn player
        val platforms = state.platforms
        if (platforms.isEmpty()) return state

        val startPlatform = platforms[0]
        val playerY = startPlatform.getYAt(50f) - GameConstants.PLAYER_SIZE

        // Reset barrel flow
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

    /**
     * Checks if the current score qualifies for the top 40.
     * Emits a UI event to show the high score dialog if it qualifies.
     */
    fun checkIfHighScore() {
        viewModelScope.launch {
            val currentScore = _gameState.value.score
            if (currentScore > 0) {
                val isHigh = checkHighScoreUseCase(currentScore)
                if (isHigh) {
                    _pendingScore = currentScore
                    _uiEvent.send(GameUiEvent.ShowHighScoreDialog(currentScore))
                }
            }
        }
    }

    /**
     * Saves the high score with the player's name.
     * @param playerName Player's name.
     * @param onComplete Callback executed after saving.
     */
    fun saveHighScore(playerName: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            saveHighScoreUseCase(playerName, _pendingScore)
            _pendingScore = 0 // Reset pending score
            onComplete()
        }
    }

    /**
     * Closes the high score dialog without saving.
     * @param onComplete Callback executed after closing.
     */
    fun dismissHighScoreDialog(onComplete: () -> Unit) {
        _pendingScore = 0 // Reset pending score
        onComplete()
    }
}
