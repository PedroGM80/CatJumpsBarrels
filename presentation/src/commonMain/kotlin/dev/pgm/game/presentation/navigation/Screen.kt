package dev.pgm.game.presentation.navigation

/**
 * Define las rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String) {
    data object MainMenu : Screen("main_menu")
    data object Game : Screen("game")
    data object HighScores : Screen("high_scores")
    data object Credits : Screen("credits")
}
