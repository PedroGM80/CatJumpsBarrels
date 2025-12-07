package dev.pgm.game.domain.usecase

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel

/**
 * Use Case: Generar barriles desde la posición del enemigo.
 * Responsabilidad única: Crear nuevos barriles en el juego.
 */
class SpawnBarrelUseCase {

    private var lastSpawnTime = 0L

    operator fun invoke(state: GameState, currentTime: Long): GameState {
        // Verificar si es tiempo de spawear un nuevo barril
        if (currentTime - lastSpawnTime < GameConstants.BARREL_SPAWN_INTERVAL) {
            return state
        }

        lastSpawnTime = currentTime

        // Crear nuevo barril en la posición del enemigo
        val newBarrel = Barrel(
            position = Offset(
                state.enemy.position.x + state.enemy.size / 2,
                state.enemy.position.y + state.enemy.size
            ),
            currentPlatformIndex = 5 // Plataforma del enemigo
        )

        return state.copy(
            barrels = state.barrels + newBarrel
        )
    }

    fun reset() {
        lastSpawnTime = 0L
    }
}
