package dev.pgm.game.di

import dev.pgm.game.domain.di.domainModule
import dev.pgm.game.presentation.di.presentationModule

/**
 * Lista de todos los módulos de la aplicación.
 * Facilita la inicialización de Koin.
 *
 * Los módulos están separados por capas:
 * - domainModule: Servicios, factories y UseCases
 * - presentationModule: ViewModels y componentes de UI
 */
val allModules = listOf(
    domainModule,
    presentationModule
)
