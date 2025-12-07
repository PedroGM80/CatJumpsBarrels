package dev.pgm.game.di

import dev.pgm.game.domain.usecase.*
import dev.pgm.game.presentation.viewmodel.GameViewModelComplete
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Módulo Koin para inyección de dependencias.
 * Koin es una librería multiplataforma que simplifica la DI sin reflexión.
 */
val gameModule = module {

    // ========== UseCases ==========
    // factoryOf crea una nueva instancia cada vez
    factoryOf(::UpdatePlayerUseCase)
    factoryOf(::UpdateSingleBarrelUseCase)
    factoryOf(::UpdateBarrelsUseCase)
    factoryOf(::CheckCollisionsUseCase)
    factoryOf(::SpawnBarrelUseCase)
    factoryOf(::UpdateParticlesUseCase)

    // ========== ViewModel ==========
    // singleOf crea una única instancia (singleton)
    singleOf(::GameViewModelComplete)
}

/**
 * Lista de todos los módulos de la aplicación.
 * Facilita la inicialización de Koin.
 */
val allModules = listOf(gameModule)
