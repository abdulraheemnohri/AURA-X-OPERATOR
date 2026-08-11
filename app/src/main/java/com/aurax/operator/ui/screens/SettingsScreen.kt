package com.aurax.operator.ui.screens
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable fun SettingsScreen(){val c=LocalContext.current;var policy by remember{mutableStateOf("CONFIRM_ACTIONS")};Column(Modifier.fillMaxSize().padding(16.dp)){Text("Settings",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(16.dp));Text("Automation policy");listOf("OBSERVE_ONLY","SUGGEST_ONLY","CONFIRM_ACTIONS","FULL_AUTO_LOW_RISK").forEach{p->Row{RadioButton(policy==p,{policy=p});Text(p,Modifier.padding(top=12.dp))}};Spacer(Modifier.height(16.dp));Button(onClick={c.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}){Text("Open Accessibility Settings")};Spacer(Modifier.height(8.dp));Button(onClick={c.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply{data=android.net.Uri.parse("package:${c.packageName}")}))){Text("Allow Floating Indicator")}}}