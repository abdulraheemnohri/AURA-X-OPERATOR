package com.aurax.operator.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurax.operator.ui.components.*
@Composable fun HomeScreen(onChat:()->Unit={}){Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){Text("AURA-X Operator",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(30.dp));AiOrb();Spacer(Modifier.height(20.dp));Text("Local-first • safety guarded • user controlled");Spacer(Modifier.height(24.dp));GlassCard(Modifier.fillMaxWidth()){Column{Text("Operator status");Text("Accessibility service must be explicitly enabled in Android Settings.")}};Spacer(Modifier.height(20.dp));Button(onClick=onChat){Text("Open Chat")}}}