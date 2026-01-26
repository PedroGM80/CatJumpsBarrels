package dev.pgm.game.di

import org.koin.core.context.startKoin

/**
 * Inicializa Koin con todos los módulos de la aplicación.
 * Debe llamarse una vez al inicio de la aplicación.
 */
fun initKoin() {
    startKoin {
        // Módulos de la aplicación
        modules(allModules)
    }
}
