package dev.pgm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.* // Importing all Material components
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp // Added for dp unit

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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "CAT JUMP BARRELS",
                style = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Web Version - Loading...",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = Color(0xFF00FFFF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}
