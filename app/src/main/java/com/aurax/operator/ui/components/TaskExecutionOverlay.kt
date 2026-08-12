package com.aurax.operator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TaskExecutionOverlay(
    step: String,
    phase: String = "EXECUTING",
    countdown: Int = 0,
    onConfirm: () -> Unit = {},
    onAbort: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = .72f)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            Modifier.padding(24.dp).widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AiOrb()
                    Spacer(Modifier.height(16.dp))
                    Text(phase, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(step, style = MaterialTheme.typography.titleMedium)

                    if (phase == "CONFIRMING") {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (countdown > 0) "Confirm in $countdown…" else "Action requires confirmation",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (countdown.coerceIn(0, 3) / 3f) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = onConfirm) { Text("Confirm") }
                            OutlinedButton(onClick = onAbort) { Text("Cancel") }
                        }
                    } else {
                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = onAbort,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFB7185))
                        ) { Text("ABORT ALL ACTIONS") }
                    }
                }
            }
        }
    }
}
