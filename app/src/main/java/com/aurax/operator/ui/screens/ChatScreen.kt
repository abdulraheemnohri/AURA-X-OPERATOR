package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.agent.execution.TaskExecutor
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.data.entities.MessageEntity
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AuraDatabase.get(context) }
    val messages by db.dao().observeMessages(TaskExecutor.CHAT_CONVERSATION_ID).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var running by remember { mutableStateOf(false) }
    var showVoice by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    if (showVoice) {
        Surface(Modifier.fillMaxSize()) {
            VoiceScreen(onBack = { showVoice = false })
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("AURA-X Operator", style = MaterialTheme.typography.headlineSmall)
                Text("Local-first automation with visible safety controls.", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showVoice = true }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice mode")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (messages.isEmpty()) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ready", style = MaterialTheme.typography.titleMedium)
                    Text("Give me a safe, supported command. Your local conversation history stays on-device.")
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                enabled = !running,
                placeholder = { Text("Try: open Chrome and search weather") }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = text.isNotBlank() && !running,
                onClick = {
                    val input = text.trim()
                    text = ""
                    running = true
                    scope.launch {
                        TaskExecutor(context).execute(input)
                        running = false
                    }
                }
            ) { Text(if (running) "Running…" else "Send") }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity) {
    val user = message.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (user) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 340.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (user) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    if (user) "You" else "AURA-X",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(3.dp))
                Text(message.content)
            }
        }
    }
}
