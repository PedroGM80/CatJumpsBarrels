package dev.pgm.game.presentation.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import dev.pgm.game.di.initKoin
import dev.pgm.game.presentation.navigation.NavigationGraph

/**
 * Punto de entrada de la aplicación con Clean Architecture + Koin + Navigation.
 * Ahora incluye sistema de navegación entre Menú, Juego, Records y Créditos.
 */
fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cat Jump Barrels",
            state = WindowState(size = DpSize(850.dp, 750.dp)),
            resizable = true
        ) {
            MaterialTheme(colors = darkColors()) {
                val navController = rememberNavController()
                NavigationGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
