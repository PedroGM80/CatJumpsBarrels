package dev.pgm.game.presentation.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.pgm.game.model.entities.HighScore
import dev.pgm.game.presentation.theme.GameFonts
import dev.pgm.game.presentation.utils.formatWithCommas
import dev.pgm.game.presentation.viewmodel.HighScoresUiState
import dev.pgm.game.presentation.viewmodel.HighScoresViewModel
import org.koin.compose.koinInject

/**
 * Pantalla de High Scores con estilo arcade retro.
 */
@Composable
fun HighScoresScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: HighScoresViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D0D1A),
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0D0D1A)
                        )
                    )
                )
        )

        // Grid de fondo
        ArcadeGrid()

        // Scanlines
        CRTScanlines()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título animado
            AnimatedScreenTitle(text = "HIGH SCORES", icon = "★")

            Spacer(modifier = Modifier.height(24.dp))

            // Cabecera de la tabla
            TableHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Contenido basado en el estado
            when (val state = uiState) {
                is HighScoresUiState.Loading -> {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = Color(0xFFFFD700),
                            strokeWidth = 3.dp
                        )
                    }
                }

                is HighScoresUiState.Success -> {
                    if (state.scores.isEmpty()) {
                        EmptyScoresMessage(modifier = Modifier.weight(1f))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                    ErrorMessage(
                        message = state.message,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de retorno
            ArcadeButton(
                text = "BACK",
                icon = "◀",
                color = Color(0xFF00BFFF),
                onClick = onNavigateBack
            )
        }
    }
}

/**
 * Grid de fondo estilo arcade.
 */
@Composable
private fun ArcadeGrid() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridSize = 40f
        val lineColor = Color(0xFFFFD700).copy(alpha = 0.03f)

        var x = 0f
        while (x < size.width) {
            drawLine(
                color = lineColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        var y = 0f
        while (y < size.height) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }
}

/**
 * Efecto scanlines CRT.
 */
@Composable
private fun CRTScanlines() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineHeight = 3f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color.Black.copy(alpha = 0.08f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += lineHeight
        }
    }
}

/**
 * Título de pantalla animado.
 */
@Composable
private fun AnimatedScreenTitle(text: String, icon: String) {
    val infiniteTransition = rememberInfiniteTransition()
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            fontSize = 32.sp,
            color = Color(0xFFFFD700)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = text,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700),
                shadow = Shadow(
                    color = Color(0xFFFF6B00).copy(alpha = glowAlpha),
                    blurRadius = 15f,
                    offset = Offset.Zero
                )
            )
        )
    }
}

/**
 * Cabecera de la tabla.
 */
@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.05f),
                        Color(0xFFFFD700).copy(alpha = 0.15f),
                        Color(0xFFFFD700).copy(alpha = 0.05f)
                    )
                ),
                RoundedCornerShape(6.dp)
            )
            .border(
                2.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.2f),
                        Color(0xFFFFD700).copy(alpha = 0.5f),
                        Color(0xFFFFD700).copy(alpha = 0.2f)
                    )
                ),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText("RANK", Modifier.width(60.dp))
        HeaderText("PLAYER", Modifier.weight(1f))
        HeaderText("SCORE", Modifier.width(90.dp), TextAlign.End)
        HeaderText("DATE", Modifier.width(100.dp), TextAlign.End)
    }
}

@Composable
private fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = GameFonts.GameFont,
            fontSize = 11.sp,
            color = Color(0xFFFFD700),
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier,
        textAlign = textAlign
    )
}

/**
 * Fila de high score.
 */
@Composable
private fun HighScoreRow(
    rank: Int,
    highScore: HighScore,
    modifier: Modifier = Modifier
) {
    val isTopThree = rank <= 3

    val (bgColor, borderColor, rankColor, medal) = when (rank) {
        1 -> listOf(
            Color(0xFFFFD700).copy(alpha = 0.12f),
            Color(0xFFFFD700).copy(alpha = 0.6f),
            Color(0xFFFFD700),
            "🥇"
        )
        2 -> listOf(
            Color(0xFFC0C0C0).copy(alpha = 0.12f),
            Color(0xFFC0C0C0).copy(alpha = 0.5f),
            Color(0xFFE8E8E8),
            "🥈"
        )
        3 -> listOf(
            Color(0xFFCD7F32).copy(alpha = 0.12f),
            Color(0xFFCD7F32).copy(alpha = 0.5f),
            Color(0xFFCD7F32),
            "🥉"
        )
        else -> listOf(
            Color(0xFF2A2A4A).copy(alpha = 0.4f),
            Color(0xFF4A4A6A).copy(alpha = 0.2f),
            Color.White.copy(alpha = 0.6f),
            ""
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor as Color)
            .border(
                width = if (isTopThree) 2.dp else 1.dp,
                color = borderColor as Color,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank con medalla
        Row(
            modifier = Modifier.width(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if ((medal as String).isNotEmpty()) {
                Text(text = medal, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = "#$rank",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 12.sp,
                    color = rankColor as Color,
                    fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Normal
                )
            )
        }

        // Nombre
        Text(
            text = highScore.playerName.uppercase(),
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 12.sp,
                color = if (isTopThree) Color.White else Color.White.copy(alpha = 0.8f),
                fontWeight = if (isTopThree) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.weight(1f)
        )

        // Score
        Text(
            text = highScore.score.formatWithCommas(),
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 12.sp,
                color = if (isTopThree) Color(0xFF00FF88) else Color(0xFF00FF88).copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.width(90.dp),
            textAlign = TextAlign.End
        )

        // Fecha
        Text(
            text = highScore.formattedDate,
            style = TextStyle(
                fontFamily = GameFonts.GameFont,
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.5f)
            ),
            modifier = Modifier.width(100.dp),
            textAlign = TextAlign.End
        )
    }
}

/**
 * Mensaje cuando no hay scores.
 */
@Composable
private fun EmptyScoresMessage(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🎮",
                fontSize = 64.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "NO SCORES YET",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 24.sp,
                    color = Color(0xFF00FFFF),
                    shadow = Shadow(
                        color = Color(0xFF00FFFF).copy(alpha = 0.5f),
                        blurRadius = 10f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Be the first to claim glory!",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

/**
 * Mensaje de error.
 */
@Composable
private fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚠",
                fontSize = 48.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "ERROR",
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 20.sp,
                    color = Color(0xFFFF6B6B),
                    shadow = Shadow(
                        color = Color(0xFFFF6B6B).copy(alpha = 0.5f),
                        blurRadius = 10f
                    )
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Botón estilo arcade.
 */
@Composable
private fun ArcadeButton(
    text: String,
    icon: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isHovered -> 1.05f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val currentColor = when {
        isPressed -> color.copy(alpha = 0.8f)
        isHovered -> color
        else -> color.copy(alpha = 0.7f)
    }

    Box(
        modifier = modifier
            .width(200.dp)
            .height(50.dp)
            .scale(scale)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF2A2A4E),
                        Color(0xFF1A1A2E)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        currentColor.copy(alpha = 0.4f),
                        currentColor,
                        currentColor.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        isHovered = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                style = TextStyle(
                    fontSize = 16.sp,
                    color = currentColor
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = text,
                style = TextStyle(
                    fontFamily = GameFonts.GameFont,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
