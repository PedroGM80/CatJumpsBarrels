package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import kotlinx.coroutines.*

/**
 * Use Case: Actualizar el estado de todas las partículas.
 * Responsabilidad única: Procesar el movimiento y aging de partículas.
 */
class UpdateParticlesUseCase {

    operator fun invoke(state: GameState, deltaTime: Float): GameState {
        // Procesar partículas en paralelo si hay más de 10 para mejor rendimiento
        val updated = if (state.particles.size > 10) {
            runBlocking(Dispatchers.Default) {
                state.particles.map { p ->
                    async {
                        val newAge = p.age + deltaTime
                        if (newAge >= p.lifetime) null
                        else p.copy(
                            position = p.position.plus(p.velocity.times(deltaTime)),
                            velocity = p.velocity.copy(
                                y = p.velocity.y + GameConstants.PARTICLE_GRAVITY * deltaTime
                            ),
                            age = newAge
                        )
                    }
                }.mapNotNull { it.await() }
            }
        } else {
            // Para pocas partículas, procesamiento secuencial es más eficiente
            state.particles.mapNotNull { p ->
                val newAge = p.age + deltaTime
                if (newAge >= p.lifetime) null
                else p.copy(
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
