package dev.pgm.game.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.pgm.game.presentation.ui.CreditsScreen
import dev.pgm.game.presentation.ui.GameScreen
import dev.pgm.game.presentation.ui.HighScoresScreen
import dev.pgm.game.presentation.ui.MainMenuScreen

/**
 * Define el grafo de navegación de la aplicación.
 */
@Composable
fun NavigationGraph(
    navController: NavHostController,
    onExitApplication: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainMenu.route,
        modifier = modifier
    ) {
        // Pantalla de Menú Principal
        composable(Screen.MainMenu.route) {
            MainMenuScreen(
                onNavigateToGame = {
                    navController.navigate(Screen.Game.route)
                },
                onNavigateToHighScores = {
                    navController.navigate(Screen.HighScores.route)
                },
                onNavigateToCredits = {
                    navController.navigate(Screen.Credits.route)
                },
                onExit = onExitApplication
            )
        }

        // Pantalla de Juego
        composable(Screen.Game.route) {
            GameScreen(
                onNavigateBack = {
                    navController.popBackStack(Screen.MainMenu.route, inclusive = false)
                },
                onNavigateToHighScores = {
                    navController.navigate(Screen.HighScores.route) {
                        popUpTo(Screen.MainMenu.route)
                    }
                }
            )
        }

        // Pantalla de High Scores
        composable(Screen.HighScores.route) {
            HighScoresScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Créditos
        composable(Screen.Credits.route) {
            CreditsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
