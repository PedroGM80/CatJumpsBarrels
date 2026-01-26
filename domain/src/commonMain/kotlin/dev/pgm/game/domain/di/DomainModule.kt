package dev.pgm.game.domain.di

import dev.pgm.game.domain.factory.ParticleFactory
import dev.pgm.game.domain.services.*
import dev.pgm.game.domain.usecase.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Módulo Koin para la capa de dominio.
 * Registra servicios, factories y use cases.
 */
val domainModule = module {
    // Services (Singletons - abstracciones para testabilidad)
    single<TimeProvider> { systemTimeProvider() }
    single<RandomProvider> { DefaultRandomProvider() }

    // Factories
    factoryOf(::ParticleFactory)

    // UseCases (Factories - nueva instancia cada vez)
    factoryOf(::UpdatePlayerUseCase)
    factoryOf(::UpdateSingleBarrelUseCase)
    factoryOf(::UpdateBarrelsUseCase)
    factoryOf(::CheckCollisionsUseCase)
    factoryOf(::SpawnBarrelUseCase)
    factoryOf(::UpdateParticlesUseCase)

    // High Score UseCases
    factoryOf(::GetTop40ScoresUseCase)
    factoryOf(::CheckHighScoreUseCase)
    factoryOf(::SaveHighScoreUseCase)
}
