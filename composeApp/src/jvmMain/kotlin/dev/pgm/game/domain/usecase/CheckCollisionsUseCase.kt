package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel
import dev.pgm.game.model.entities.PlayerState
import dev.pgm.game.model.utils.GameRect
import dev.pgm.game.model.utils.Particle
import dev.pgm.game.model.utils.ScorePopup
import kotlin.random.Random

/**
 * Use Case: Detectar y manejar colisiones entre el jugador y los barriles.
 * Responsabilidad única: Lógica de detección de colisiones y consecuencias.
 */
class CheckCollisionsUseCase {

    operator fun invoke(state: GameState): GameState {
        if (state.player.state == PlayerState.DEAD) return state

        val playerRect = GameRect(
            state.player.position.x,
            state.player.position.y,
            state.player.size,
            state.player.size
        )

        for (barrel in state.barrels) {
            val barrelRect = GameRect(
                barrel.position.x,
                barrel.position.y,
                barrel.size,
                barrel.size
            )

            if (!playerRect.overlaps(barrelRect)) continue

            // Verificar si el jugador saltó sobre el barril
            if (canJumpOverBarrel(state.player.position.y, barrel, state.player.size)) {
                if (!barrel.hasBeenJumped) {
                    return handleBarrelJumped(state, barrel)
                }
            } else {
                // Colisión mortal
                return handlePlayerDeath(state)
            }
        }

        return state
    }

    private fun canJumpOverBarrel(playerY: Float, barrel: Barrel, playerSize: Float): Boolean {
        val playerBottom = playerY + playerSize
        val barrelTop = barrel.position.y
        val barrelBottom = barrel.position.y + barrel.size

        return playerBottom >= barrelTop - GameConstants.BARREL_JUMP_TOLERANCE_TOP &&
                playerBottom <= barrelBottom + GameConstants.BARREL_JUMP_TOLERANCE_BOTTOM
    }

    private fun handleBarrelJumped(state: GameState, barrel: Barrel): GameState {
        return state.copy(
            score = state.score + GameConstants.POINTS_JUMP_BARREL,
            barrels = state.barrels.map {
                if (it === barrel) it.copy(hasBeenJumped = true) else it
            },
            particles = state.particles + createScoreParticles(barrel.position.x, barrel.position.y),
            lastScorePopup = ScorePopup(barrel.position, GameConstants.POINTS_JUMP_BARREL)
        )
    }

    private fun handlePlayerDeath(state: GameState): GameState {
        val newLives = state.lives - 1
        val isGameOver = newLives <= 0

        return state.copy(
            player = state.player.copy(state = PlayerState.DEAD),
            lives = newLives,
            isGameOver = isGameOver,
            playerDeathTimestamp = System.currentTimeMillis(),
            particles = state.particles + createDeathParticles(
                state.player.position.x,
                state.player.position.y
            )
        )
    }

    private fun createScoreParticles(x: Float, y: Float): List<Particle> {
        return List(GameConstants.SCORE_PARTICLE_COUNT) {
            Particle(
                position = androidx.compose.ui.geometry.Offset(
                    x + Random.nextFloat() * 20f,
                    y + Random.nextFloat() * 20f
                ),
                velocity = androidx.compose.ui.geometry.Offset(
                    (Random.nextFloat() - 0.5f) * GameConstants.SCORE_PARTICLE_VEL_X_RANGE,
                    Random.nextFloat() * GameConstants.SCORE_PARTICLE_VEL_Y_RANGE
                ),
                color = androidx.compose.ui.graphics.Color(
                    Random.nextFloat(),
                    Random.nextFloat(),
                    Random.nextFloat(),
                    1f
                ).value.toLong(),
                size = 2f + Random.nextFloat() * GameConstants.SCORE_PARTICLE_SIZE_RANGE,
                lifetime = GameConstants.SCORE_PARTICLE_LIFETIME,
                age = 0f
            )
        }
    }

    private fun createDeathParticles(x: Float, y: Float): List<Particle> {
        return List(GameConstants.DEATH_PARTICLE_COUNT) {
            Particle(
                position = androidx.compose.ui.geometry.Offset(
                    x + Random.nextFloat() * 30f,
                    y + Random.nextFloat() * 30f
                ),
                velocity = androidx.compose.ui.geometry.Offset(
                    (Random.nextFloat() - 0.5f) * GameConstants.DEATH_PARTICLE_VEL_X_RANGE,
                    Random.nextFloat() * GameConstants.DEATH_PARTICLE_VEL_Y_RANGE
                ),
                color = androidx.compose.ui.graphics.Color.Red.value.toLong(),
                size = 3f + Random.nextFloat() * GameConstants.DEATH_PARTICLE_SIZE_RANGE,
                lifetime = GameConstants.DEATH_PARTICLE_LIFETIME,
                age = 0f
            )
        }
    }
}
