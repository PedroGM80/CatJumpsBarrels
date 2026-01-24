package dev.pgm.game.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.pgm.game.audio.MusicController
import dev.pgm.game.audio.SoundPlayer
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
            // Iniciar música del menú cuando se entra
            DisposableEffect(Unit) {
                MusicController.playMenuMusic()
                onDispose {
                    MusicController.stopMusic()
                }
            }

            MainMenuScreen(
                onNavigateToGame = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    navController.navigate(Screen.Game.route)
                },
                onNavigateToHighScores = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    navController.navigate(Screen.HighScores.route)
                },
                onNavigateToCredits = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    navController.navigate(Screen.Credits.route)
                },
                onExit = {
                    SoundPlayer.playMenuConfirm()
                    MusicController.stopMusic()
                    onExitApplication()
                }
            )
        }

        // Pantalla de Juego
        composable(Screen.Game.route) {
            // Asegurar que la música esté parada al entrar
            LaunchedEffect(Unit) {
                MusicController.stopMusic()
            }
            
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
            // Asegurar que la música esté parada al entrar
            LaunchedEffect(Unit) {
                MusicController.stopMusic()
            }
            
            HighScoresScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de Créditos
        composable(Screen.Credits.route) {
            // Asegurar que la música esté parada al entrar
            LaunchedEffect(Unit) {
                MusicController.stopMusic()
            }
            
            CreditsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
