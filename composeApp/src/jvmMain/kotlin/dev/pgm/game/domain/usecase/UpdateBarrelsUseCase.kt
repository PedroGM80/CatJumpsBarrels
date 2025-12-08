package dev.pgm.game.domain.usecase

import dev.pgm.game.model.core.GameState

/**
 * Use Case: Actualizar el estado de todos los barriles.
 * Responsabilidad única: Procesar el movimiento y comportamiento de los barriles.
 *
 * Refactored: Removed runBlocking that was blocking UI thread
 */
class UpdateBarrelsUseCase(
    private val updateSingleBarrelUseCase: UpdateSingleBarrelUseCase
) {

    operator fun invoke(state: GameState): GameState {
        // Process all barrels sequentially
        // Note: For true parallelism, the entire game loop would need to be refactored to use coroutines
        val updatedBarrels = state.barrels.mapNotNull { barrel ->
            updateSingleBarrelUseCase(barrel, state)
        }

        return state.copy(barrels = updatedBarrels)
    }
}
