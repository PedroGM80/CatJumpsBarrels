package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel
import kotlinx.coroutines.*

/**
 * Use Case: Actualizar el estado de todos los barriles.
 * Responsabilidad única: Procesar el movimiento y comportamiento de los barriles.
 */
class UpdateBarrelsUseCase(
    private val updateSingleBarrelUseCase: UpdateSingleBarrelUseCase
) {

    operator fun invoke(state: GameState): GameState {
        // Procesar barriles en paralelo si hay más de 3 para mejor rendimiento
        val updatedBarrels = if (state.barrels.size > 3) {
            runBlocking(Dispatchers.Default) {
                state.barrels.map { barrel ->
                    async {
                        updateSingleBarrelUseCase(barrel, state)
                    }
                }.mapNotNull { it.await() }
            }
        } else {
            // Para pocos barriles, procesamiento secuencial es más eficiente
            state.barrels.mapNotNull { barrel ->
                updateSingleBarrelUseCase(barrel, state)
            }
        }

        return state.copy(barrels = updatedBarrels)
    }
}
