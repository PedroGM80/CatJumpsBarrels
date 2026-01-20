package dev.pgm.game.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.presentation.theme.GameFonts

/**
 * Pantalla del menú principal.
 * Muestra tres opciones: Nuevo Juego, Records y Créditos.
 */
@Composable
fun MainMenuScreen(
    onNavigateToGame: () -> Unit,
    onNavigateToHighScores: () -> Unit,
    onNavigateToCredits: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Fondo oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A1A))
        )

        // Contenido del menú
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título del juego
            Text(
                text = "CAT JUMP\nBARRELS",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 42.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botones del menú
            MenuButton(
                text = "NEW GAME",
                onClick = onNavigateToGame
            )

            MenuButton(
                text = "HIGH SCORES",
                onClick = onNavigateToHighScores
            )

            MenuButton(
                text = "CREDITS",
                onClick = onNavigateToCredits
            )
        }

        // Versión en la esquina
        Text(
            text = "v1.0.0",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

/**
 * Botón del menú con estilo retro arcade.
 */
@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(280.dp)
            .height(60.dp)
            .border(
                width = 3.dp,
                color = if (isPressed) Color(0xFFFFD700) else Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                color = if (isPressed) Color(0xFF333333) else Color(0xFF1A1A1A),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onClick()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 18.sp,
                color = if (isPressed) Color(0xFFFFD700) else Color.White,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
