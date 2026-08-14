# AURA-X NEXUS — Next Implementation Pass

Date: 2026-08-14

## Implemented

- Verified the existing llama.cpp JNI brain: `LlamaCppRuntime.kt` is present and loads `aurax_native`.
- Verified the Room DAO: `AuraDao` is declared inside `AuraDatabase.kt`; a separate `AuraDao.kt` file is not required.
- Verified the native LLM bridge: `app/src/main/cpp/native-lib.cpp` and its CMake target are present.
- Added `whisper-native.cpp` JNI bridge for offline Whisper transcription.
- Added whisper.cpp v1.9.1 as a CPU-only native dependency for Android arm64-v8a.
- Replaced `WhisperRecognizer` placeholder behavior with real `AudioRecord` capture at 16 kHz and Whisper JNI inference.
- Updated `VoiceScreen` to prefer local Whisper when `sttModelPath` points to a real model, with Android `SpeechRecognizer` fallback when it is not available.
- Replaced the `PiperTTS` false placeholder with a functional on-device Android TTS compatibility adapter.
- Replaced Model Hub Material cards with the existing AURA-X `GlassCard` component.
- Upgraded encrypted backup format to version 2 with random salt and IV.
- Backup now includes recent chat messages, memories, safety events and primitive local settings.
- Added password-authenticated, non-destructive backup restore/merge with legacy v1 backup compatibility.

## Explicitly not claimed

- No embedded Whisper model asset is bundled automatically. The user must select/provide a compatible Whisper model file.
- `PiperTTS` is a functional local Android TTS compatibility backend; a native Piper engine/model remains optional.
- Native Android build, CMake compilation and physical-device microphone/Whisper tests still require a real Android build runner/device.

## Existing gaps already addressed earlier

- Chat history is persisted and rendered through `LazyColumn`.
- Task execution already retrieves matching memories and passes them into both planners.
- Room already uses explicit migrations and does not rely on destructive migration fallback.
- Local RAG context is already wired into the Chat execution path.
