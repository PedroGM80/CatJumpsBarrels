package dev.pgm.game.presentation.di

import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import dev.pgm.game.presentation.viewmodel.HighScoresViewModel
import dev.pgm.game.presentation.viewmodel.MainMenuViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin para la capa de presentación.
 * Registra ViewModels y componentes de UI.
 */
val presentationModule = module {
    // ViewModels (Singletons)
    singleOf(::GameViewModelComplete)
    singleOf(::MainMenuViewModel)
    singleOf(::HighScoresViewModel)
}
