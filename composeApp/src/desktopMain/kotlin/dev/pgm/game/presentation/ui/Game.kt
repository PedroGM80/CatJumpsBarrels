package dev.pgm.game.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.navigation.compose.rememberNavController
import dev.pgm.game.di.initKoin
import dev.pgm.game.presentation.navigation.NavigationGraph
import dev.pgm.game.resources.AnimationProvider
import kotlinx.coroutines.launch

/**
 * Punto de entrada de la aplicación con Clean Architecture + Koin + Navigation.
 * Carga los recursos usando Compose Multiplatform Resources API.
 */
fun main() {
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Cat Jump Barrels",
            state = WindowState(placement = WindowPlacement.Fullscreen),
            resizable = true
        ) {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var isLoading by remember { mutableStateOf(true) }
                
                // Cargar animaciones al iniciar
                LaunchedEffect(Unit) {
                    launch {
                        AnimationProvider.loadAllAnimations()
                        isLoading = false
                    }
                }
                
                if (isLoading) {
                    // Pantalla de carga
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                    }
                } else {
                    val navController = rememberNavController()
                    NavigationGraph(
                        navController = navController,
                        onExitApplication = ::exitApplication,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
