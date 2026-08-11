package com.aurax.operator.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable fun TaskScreen(){Column(Modifier.fillMaxSize().padding(16.dp)){Text("Tasks",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(12.dp));Text("No queued tasks.");Text("Every operator action is intended to be auditable in the local Room database.")}}