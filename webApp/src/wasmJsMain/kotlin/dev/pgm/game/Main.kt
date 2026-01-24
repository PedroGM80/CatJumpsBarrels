package dev.pgm.game

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.pgm.game.data.di.dataModule
import dev.pgm.game.domain.di.domainModule
import dev.pgm.game.presentation.di.presentationModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // Inicializar Koin
    startKoin {
        modules(
            dataModule,
            domainModule,
            presentationModule
        )
    }
    
    CanvasBasedWindow(canvasElementId = "root", title = "Cat Jump Barrels") {
        WebApp()
    }
}
