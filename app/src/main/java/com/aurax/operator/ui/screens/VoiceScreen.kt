package com.aurax.operator.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.security.SecurePrefs
import com.aurax.operator.voice.VoiceOutput
import com.aurax.operator.voice.stt.WhisperRecognizer
import java.io.File
import java.util.Locale

@Composable
fun VoiceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs(context) }
    val voice = remember { VoiceOutput(context) }
    val whisper = remember { WhisperRecognizer() }
    var transcript by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Hold to speak") }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var usingWhisper by remember { mutableStateOf(false) }
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
        status = if (granted) "Hold to speak" else "Microphone permission is required"
    }

    fun languageTag(): String = when (prefs.sttLanguage.lowercase(Locale.US)) {
        "ur" -> "ur-PK"
        "hi" -> "hi-IN"
        "en" -> "en-US"
        else -> "auto"
    }

    fun startSystemRecognizer(language: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            status = "Speech recognition is unavailable on this device"
            return
        }
        val r = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer?.destroy()
        recognizer = r
        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { status = "Listening online…"; listening = true }
            override fun onBeginningOfSpeech() { status = "Listening online…" }
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { listening = false; status = "Processing…" }
            override fun onError(error: Int) {
                listening = false
                status = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy; try again"
                    else -> "Speech recognition error ($error)"
                }
            }
            override fun onResults(results: Bundle?) {
                listening = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                transcript = matches?.firstOrNull().orEmpty()
                status = if (transcript.isBlank()) "No speech recognized" else "Transcript ready"
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) transcript = partial
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "auto") Locale.getDefault().toLanguageTag() else language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        r.startListening(intent)
    }

    fun startListening() {
        if (!permissionGranted) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        val modelPath = prefs.sttModelPath
        val language = languageTag()
        if (modelPath.isNotBlank() && File(modelPath).isFile && whisper.isAvailable()) {
            usingWhisper = whisper.start(modelPath, language) { result ->
                transcript = result
                listening = false
                usingWhisper = false
                status = if (result.isBlank()) "No speech recognized" else "Offline transcript ready"
            }
            if (usingWhisper) {
                listening = true
                status = "Listening offline…"
                return
            }
        }

        usingWhisper = false
        startSystemRecognizer(language)
    }

    fun stopListening() {
        if (usingWhisper) {
            whisper.stop()
            usingWhisper = false
            listening = false
            status = "Processing offline…"
            return
        }
        recognizer?.stopListening()
        listening = false
        status = "Processing…"
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizer?.destroy()
            whisper.stop()
            voice.shutdown()
        }
    }

    val pulse by rememberInfiniteTransition(label = "voice").animateFloat(
        initialValue = 0.92f,
        targetValue = if (listening) 1.12f else 1.02f,
        animationSpec = infiniteRepeatable(tween(if (listening) 500 else 1100), RepeatMode.Reverse),
        label = "pulse"
    )

    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF182A3A), Color(0xFF050508)), center = center, radius = size.maxDimension))
            for (i in 0 until 24) {
                val a = i * (Math.PI * 2.0 / 24.0)
                val r = 100f + (i % 3) * 28f
                drawCircle(Color(0x665EEAD4), 3f, Offset(center.x + kotlin.math.cos(a).toFloat() * r, center.y + kotlin.math.sin(a).toFloat() * r))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("AURA-X Voice", style = MaterialTheme.typography.headlineMedium)
            Box(Modifier.size((200 * pulse).dp), contentAlignment = Alignment.Center) {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = if (listening) Color(0xFF234A43) else Color(0xFF162B35), shadowElevation = 18.dp) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("A", style = MaterialTheme.typography.displayLarge) }
                }
            }
            AssistChip(onClick = {}, label = { Text("Operator: ${AppState.operator.value.phase}") })
            AssistChip(onClick = {}, label = { Text(if (usingWhisper) "STT: Whisper offline" else "STT: Android fallback") })
            Text(status, style = MaterialTheme.typography.bodyLarge)
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Transcript", style = MaterialTheme.typography.labelLarge)
                    Text(transcript.ifBlank { "Your speech will appear here." }, style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (!listening) {
                Button(
                    onClick = { if (!permissionGranted) permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.pointerInput(permissionGranted) {
                        detectTapGestures(onPress = {
                            startListening()
                            if (permissionGranted) tryAwaitRelease()
                            if (listening) stopListening()
                        })
                    }
                ) { Text(if (permissionGranted) "Hold to Speak" else "Grant Microphone") }
            } else {
                Button(onClick = { stopListening() }) { Text("Stop Listening") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = transcript.isNotBlank(), onClick = { voice.speak(transcript) }) { Text("Speak transcript") }
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    }
}
