package dev.pgm.game.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.pgm.game.presentation.theme.GameFonts

@Composable
fun HighScoreDialog(
    score: Int,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D2D2D)),
            modifier = Modifier.padding(16.dp).width(300.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NEW HIGH SCORE!",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        fontFamily = GameFonts.GameFont
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "Score: $score",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontFamily = GameFonts.GameFont
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { 
                        if (it.length <= 10) {
                            playerName = it
                            isError = false
                        }
                    },
                    label = { Text("Enter your name") },
                    singleLine = true,
                    isError = isError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color(0xFFFFD700)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (isError) {
                    Text(
                        "Name cannot be empty",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start).padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip", color = Color.Gray)
                    }
                    
                    Button(
                        onClick = {
                            if (playerName.isNotBlank()) {
                                onSave(playerName)
                            } else {
                                isError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                    ) {
                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
