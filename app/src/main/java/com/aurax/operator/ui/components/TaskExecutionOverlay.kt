package com.aurax.operator.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
@Composable fun TaskExecutionOverlay(step:String,onAbort:()->Unit){Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.72f)),contentAlignment=Alignment.TopCenter){Column(Modifier.padding(32.dp),horizontalAlignment=Alignment.CenterHorizontally){AiOrb();Spacer(Modifier.height(18.dp));Text(step);Spacer(Modifier.height(18.dp));Button(onClick=onAbort,colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFB7185))){Text("ABORT ALL ACTIONS")}}}}