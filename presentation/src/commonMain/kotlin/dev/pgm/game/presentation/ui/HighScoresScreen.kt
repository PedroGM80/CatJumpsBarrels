package dev.pgm.game.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.model.entities.HighScore
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.presentation.viewmodel.HighScoresUiState
import dev.pgm.game.presentation.viewmodel.HighScoresViewModel
import org.koin.compose.koinInject

/**
 * Pantalla de High Scores que muestra los top 40 mejores puntajes.
 */
@Composable
fun HighScoresScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HighScoresViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "HIGH SCORES",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 32.sp,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Contenido basado en el estado
            when (val state = uiState) {
                is HighScoresUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFD700))
                    }
                }

                is HighScoresUiState.Success -> {
                    if (state.scores.isEmpty()) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No high scores yet.\nBe the first!",
                                style = TextStyle(
                                    fontFamily = GameFonts.GameFont,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(state.scores) { index, highScore ->
                                HighScoreRow(
                                    rank = index + 1,
                                    highScore = highScore
                                )
                            }
                        }
                    }
                }

                is HighScoresUiState.Error -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${state.message}",
                            style = TextStyle(
                                fontFamily = GameFonts.GameFont,
                                fontSize = 14.sp,
                                color = Color.Red
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
 * Fila individual de high score.
 */
@Composable
private fun HighScoreRow(
    rank: Int,
    highScore: HighScore,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f)  // Oro
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)  // Plata
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)  // Bronce
        else -> Color(0xFF333333).copy(alpha = 0.5f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ranking
        Text(
            text = "$rank.",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 14.sp,
                color = Color.White
            ),
            modifier = Modifier.width(40.dp)
        )

        // Nombre del jugador
        Text(
            text = highScore.playerName,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 14.sp,
                color = Color.White
            ),
            modifier = Modifier.weight(1f)
        )

        // Score
        Text(
            text = "${highScore.score}",
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 14.sp,
                color = Color(0xFFFFD700)
            ),
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End
        )

        // Fecha
        Text(
            text = highScore.formattedDate,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.width(120.dp),
            textAlign = TextAlign.End
        )
    }
}
