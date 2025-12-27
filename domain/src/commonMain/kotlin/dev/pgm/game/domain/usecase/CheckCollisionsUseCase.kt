package dev.pgm.game.domain.usecase

import dev.pgm.game.domain.factory.ParticleFactory
import dev.pgm.game.domain.services.TimeProvider
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel
import dev.pgm.game.model.entities.PlayerState
import dev.pgm.game.core.utils.GameRect
import dev.pgm.game.model.utils.ScorePopup

/**
 * Use Case: Detectar y manejar colisiones entre el jugador y los barriles.
 * Responsabilidad única: Lógica de detección de colisiones y consecuencias.
 *
 * Refactored to follow Dependency Inversion Principle (SOLID)
 */
class CheckCollisionsUseCase(
    private val particleFactory: ParticleFactory,
    private val timeProvider: TimeProvider
) {

    operator fun invoke(state: GameState): GameState {
        if (state.player.state == PlayerState.DEAD) return state
        if (state.player.isInvincible) return state // No colisión si es invencible

        // Usar hitbox reducida del jugador para colisión más precisa
        val playerHitbox = state.player.hitbox

        for (barrel in state.barrels) {
            // Hitbox reducida del barril para mejor jugabilidad
            val barrelHitbox = GameRect(
                barrel.position.x + 2f,
                barrel.position.y + 2f,
                barrel.size - 4f,
                barrel.size - 4f
            )

            if (!playerHitbox.overlaps(barrelHitbox)) continue

            // Verificar si el jugador saltó sobre el barril
            // Solo cuenta si está cayendo desde arriba
            val isJumpingOverBarrel = canJumpOverBarrel(state.player, barrel)

            if (isJumpingOverBarrel && !barrel.hasBeenJumped) {
                return handleBarrelJumped(state, barrel)
            } else if (!isJumpingOverBarrel) {
                // Colisión mortal (lateral, frontal, o barril cayendo sobre jugador)
                return handlePlayerDeath(state)
            }
        }

        return state
    }

    private fun canJumpOverBarrel(player: dev.pgm.game.model.entities.Player, barrel: Barrel): Boolean {
        // Solo puede saltar sobre el barril si está cayendo desde arriba
        val isFallingOrJumping = player.state == PlayerState.FALLING ||
                                 player.state == PlayerState.JUMPING ||
                                 player.velocity.y > 0

        if (!isFallingOrJumping) return false

        val playerBottom = player.position.y + player.size
        val barrelTop = barrel.position.y

        // El jugador debe estar cayendo sobre el barril desde arriba
        return playerBottom <= barrelTop + GameConstants.BARREL_JUMP_TOLERANCE_TOP &&
               playerBottom >= barrelTop - GameConstants.BARREL_JUMP_TOLERANCE_TOP
    }

    private fun handleBarrelJumped(state: GameState, barrel: Barrel): GameState {
        // Eliminar el barril de la lista cuando se salta sobre él
        val remainingBarrels = state.barrels.filter { it !== barrel }

        return state.copy(
            score = state.score + GameConstants.POINTS_JUMP_BARREL,
            barrels = remainingBarrels,
            particles = state.particles + particleFactory.createParticles(
                barrel.position.x + barrel.size / 2,  // Centro del barril
                barrel.position.y + barrel.size / 2,
                ParticleFactory.ParticleType.BARREL_DESTROYED  // Fragmentos de barril con colores marrones
            ),
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
            playerDeathTimestamp = timeProvider.currentTimeMillis(),
            barrels = emptyList(), // Limpiar todos los barriles
            particles = state.particles + particleFactory.createParticles(
                state.player.position.x + state.player.size / 2,  // Centro del jugador
                state.player.position.y + state.player.size / 2,
                ParticleFactory.ParticleType.DEATH
            )
        )
    }
}
