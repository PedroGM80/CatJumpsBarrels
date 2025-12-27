package dev.pgm.game.presentation.di

import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin para la capa de presentación.
 * Registra ViewModels y componentes de UI.
 */
val presentationModule = module {
    // ViewModel (Singleton)
    singleOf(::GameViewModelComplete)
}
