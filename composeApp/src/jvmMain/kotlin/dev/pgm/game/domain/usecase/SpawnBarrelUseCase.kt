package dev.pgm.game.domain.usecase

import androidx.compose.ui.geometry.Offset
import dev.pgm.game.model.core.GameConstants
import dev.pgm.game.model.core.GameState
import dev.pgm.game.model.entities.Barrel
import dev.pgm.game.model.entities.BossState

/**
 * Use Case: Generar barriles desde la posición del enemigo.
 * Responsabilidad única: Crear nuevos barriles en el juego.
 */
class SpawnBarrelUseCase {

    private var lastSpawnTime = 0L
    private var barrelSpawnedInCurrentThrow = false

    operator fun invoke(state: GameState, currentTime: Long): GameState {
        val boss = state.enemy

        // Si el Boss está en IDLE, verificar si es tiempo de iniciar un nuevo lanzamiento
        if (boss.state == BossState.IDLE) {
            if (currentTime - lastSpawnTime < GameConstants.BARREL_SPAWN_INTERVAL) {
                return state
            }

            // Iniciar animación de lanzamiento
            lastSpawnTime = currentTime
            barrelSpawnedInCurrentThrow = false
            return state.copy(
                enemy = boss.copy(state = BossState.THROWING, animationFrame = 0)
            )
        }

        if (boss.state == BossState.THROWING && !barrelSpawnedInCurrentThrow) {
            if (boss.animationFrame >= 6) {
                barrelSpawnedInCurrentThrow = true

                val newBarrel = Barrel(
                    position = Offset(
                        boss.position.x + boss.size / 2,
                        boss.position.y + boss.size
                    ),
                    currentPlatformIndex = 5
                )

                return state.copy(barrels = state.barrels + newBarrel)
            }
        }

        return state
    }

    fun reset() {
        lastSpawnTime = 0L
        barrelSpawnedInCurrentThrow = false
    }
}
