package dev.pgm.game.data.di

import dev.pgm.game.data.database.GameDatabase
import dev.pgm.game.data.repository.HighScoreRepositoryImpl
import dev.pgm.game.domain.repository.HighScoreRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Módulo Koin para la capa de datos.
 * Registra la base de datos, DAOs y repositorios.
 */
val dataModule = module {
    // Database (Singleton)
    single {
        GameDatabase.create()
    }

    // DAOs
    single {
        get<GameDatabase>().highScoreDao()
    }

    // Repositories
    singleOf(::HighScoreRepositoryImpl) bind HighScoreRepository::class
}
