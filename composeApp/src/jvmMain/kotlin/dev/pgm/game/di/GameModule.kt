package dev.pgm.game.di

import dev.pgm.game.domain.usecase.*
import dev.pgm.game.presentation.viewmodel.GameViewModel

/**
 * Módulo de inyección de dependencias manual.
 * Inspirado en el enfoque de Antonio Leiva para proyectos pequeños/medianos.
 * Proporciona todas las dependencias del juego sin necesidad de una librería de DI.
 */
object GameModule {

    /**
     * Crea una instancia del GameViewModel con todas sus dependencias.
     * Este es el punto de entrada principal para obtener el ViewModel configurado.
     */
    fun provideGameViewModel(): GameViewModel {
        return GameViewModel(
            updatePlayerUseCase = provideUpdatePlayerUseCase(),
            updateBarrelsUseCase = provideUpdateBarrelsUseCase(),
            checkCollisionsUseCase = provideCheckCollisionsUseCase()
        )
    }

    // ========== Use Cases ==========

    private fun provideUpdatePlayerUseCase(): UpdatePlayerUseCase {
        return UpdatePlayerUseCase()
    }

    private fun provideUpdateBarrelsUseCase(): UpdateBarrelsUseCase {
        return UpdateBarrelsUseCase(
            updateSingleBarrelUseCase = provideUpdateSingleBarrelUseCase()
        )
    }

    private fun provideUpdateSingleBarrelUseCase(): UpdateSingleBarrelUseCase {
        return UpdateSingleBarrelUseCase()
    }

    private fun provideCheckCollisionsUseCase(): CheckCollisionsUseCase {
        return CheckCollisionsUseCase()
    }
}
