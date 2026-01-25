package dev.pgm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Minimal Composable for testing basic Compose/wasmJs setup
 */
@Composable
fun MinimalApp() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.sp.value.dp)
        ) {
            Text(
                text = "CAT JUMP BARRELS",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            )

            Spacer(modifier = Modifier.height(24.sp.value.dp))

            Text(
                text = "Web Version - Loading...",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = Color(0xFF00FFFF)
                )
            )

            Spacer(modifier = Modifier.height(16.sp.value.dp))

            Text(
                text = "Game compiled for wasmJs",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            )
        }
    }
}
