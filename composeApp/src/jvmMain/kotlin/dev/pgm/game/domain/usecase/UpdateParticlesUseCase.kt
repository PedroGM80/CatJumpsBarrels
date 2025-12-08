package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState

/**
 * Use Case: Actualizar el estado de todas las partículas.
 * Responsabilidad única: Procesar el movimiento y aging de partículas.
 *
 * Refactored: Removed runBlocking that was blocking UI thread
 */
class UpdateParticlesUseCase {

    operator fun invoke(state: GameState, deltaTime: Float): GameState {
        // Process all particles sequentially
        // Note: For true parallelism, the entire game loop would need to be refactored to use coroutines
        val updated = state.particles.mapNotNull { p ->
            val newAge = p.age + deltaTime
            if (newAge >= p.lifetime) {
                null // Particle expired
            } else {
                p.copy(
                    position = p.position.plus(p.velocity.times(deltaTime)),
                    velocity = p.velocity.copy(
                        y = p.velocity.y + GameConstants.PARTICLE_GRAVITY * deltaTime
                    ),
                    age = newAge
                )
            }
        }

        return state.copy(particles = updated)
    }
}
