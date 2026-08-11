package com.aurax.operator.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurax.operator.agent.planner.OperatorPlanner
@Composable fun ChatScreen(){var text by remember{mutableStateOf("")};var reply by remember{mutableStateOf("Ready. Give me a safe, supported command.")};Column(Modifier.fillMaxSize().padding(16.dp)){Text("Chat",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(16.dp));Text(reply);Spacer(Modifier.weight(1f));Row{TextField(text,{text=it},Modifier.weight(1f),placeholder={Text("Try: open Chrome and search weather")});Spacer(Modifier.width(8.dp));Button(onClick={reply=OperatorPlanner().plan(text).joinToString{it.description}}){Text("Send")}}}}