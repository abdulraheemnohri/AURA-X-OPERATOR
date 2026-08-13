package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aurax.operator.data.entities.MessageEntity
import com.aurax.operator.ui.components.GlassCard
import com.aurax.operator.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AURA-X Operator", style = MaterialTheme.typography.headlineSmall)
                Text("Local conversation history and guarded task execution.", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { /* Voice is available from the voice screen. */ }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice")
            }
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                when (message.role) {
                    "user" -> UserMessageBubble(message.content)
                    "assistant" -> AiMessageBubble(message.content)
                    else -> AiMessageBubble(message.content)
                }
            }
            if (isLoading) item { LoadingIndicator() }
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                placeholder = { Text("Try: YouTube pe cats search karo") },
                singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                enabled = !isLoading && input.isNotBlank(),
                onClick = {
                    viewModel.sendMessage(input)
                    input = ""
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        GlassCard(modifier = Modifier.widthIn(max = 330.dp), emphasized = true) {
            Text(text)
        }
    }
}

@Composable
private fun AiMessageBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        GlassCard(modifier = Modifier.widthIn(max = 360.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("AURA-X", style = MaterialTheme.typography.labelSmall)
                Text(text)
            }
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        AssistChip(onClick = {}, label = { Text("AURA-X is working…") })
    }
}
