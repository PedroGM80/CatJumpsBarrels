package dev.pgm.game.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.presentation.theme.GameFonts

/**
 * Pantalla de créditos que muestra información sobre el juego.
 */
@Composable
fun CreditsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Fondo oscuro
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "CREDITS",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 32.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Desarrollador
            CreditSection(
                title = "DEVELOPER",
                content = listOf("Pedro GM")
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tecnologías
            CreditSection(
                title = "TECHNOLOGIES",
                content = listOf(
                    "Kotlin Multiplatform",
                    "Jetpack Compose Desktop",
                    "Clean Architecture",
                    "Room Database",
                    "Koin DI",
                    "Coroutines"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Controles
            CreditSection(
                title = "CONTROLS",
                content = listOf(
                    "WASD or Arrow Keys - Move",
                    "W or Up - Climb Up",
                    "S or Down - Climb Down",
                    "SPACE - Jump",
                    "P or ESC - Pause",
                    "R - Restart"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Atribuciones
            CreditSection(
                title = "ATTRIBUTIONS",
                content = listOf(
                    "Font: Press Start 2P",
                    "by Google Fonts",
                    "Inspired by Donkey Kong (1981)",
                    "Original concept by Nintendo"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Enlaces
            CreditSection(
                title = "LINKS",
                content = listOf(
                    "GitHub Repository",
                    "github.com/pgm/catjumpbarrels",
                    "License: MIT"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Versión
            Text(
                text = "Version 1.0.0",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de retorno
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Color.White
                ),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Text(
                    text = "BACK",
                    style = TextStyle(
                        fontFamily = GameFonts.GameFont,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

/**
 * Sección de créditos con título y contenido.
 */
@Composable
private fun CreditSection(
    title: String,
    content: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 18.sp,
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        content.forEach { line ->
            Text(
                text = line,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 12.sp,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
