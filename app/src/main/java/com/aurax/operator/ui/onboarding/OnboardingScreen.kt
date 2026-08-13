package com.aurax.operator.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val controller = remember { OnboardingController(LocalContext.current) }
    var step by remember { mutableStateOf(controller.step) }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AURA-X Setup", style = MaterialTheme.typography.headlineLarge)
        Text(step.name.replace('_',' '), style = MaterialTheme.typography.titleLarge)
        Text("Complete the local setup sequence before opening the operator cockpit.")
        Button(onClick = { controller.advance(); step = controller.step; if(controller.completed) onComplete() }, Modifier.fillMaxWidth()) { Text(if(step==OnboardingController.Step.VOICE) "Finish setup" else "Continue") }
        if(step != OnboardingController.Step.WELCOME) OutlinedButton(onClick = { controller.back(); step = controller.step }, Modifier.fillMaxWidth()) { Text("Back") }
        TextButton(onClick = { controller.skipToComplete(); onComplete() }, Modifier.fillMaxWidth()) { Text("Skip for now") }
    }
}
