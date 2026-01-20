package dev.pgm.game.di

import dev.pgm.game.data.di.dataModule
import dev.pgm.game.domain.di.domainModule
import dev.pgm.game.presentation.di.presentationModule

/**
 * Lista de todos los módulos de la aplicación.
 * Facilita la inicialización de Koin.
 *
 * Los módulos están separados por capas:
 * - dataModule: Base de datos, DAOs y Repositorios
 * - domainModule: Servicios, factories y UseCases
 * - presentationModule: ViewModels y componentes de UI
 */
val allModules = listOf(
    dataModule,
    domainModule,
    presentationModule
)
