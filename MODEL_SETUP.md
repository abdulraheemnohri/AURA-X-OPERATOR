# AURA-X local model setup

AURA-X is designed to keep inference local. Model weights are deliberately not committed to Git.

## Text model

Recommended baseline: `Qwen/Qwen2.5-0.5B-Instruct-GGUF` from Hugging Face.

- Hub: https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF
- License: Apache-2.0
- Use a Q4_K_M GGUF variant sized for the device.
- Place the selected `.gguf` under the app's private model storage and expose it through the native llama.cpp bridge.

The Java/Kotlin runtime intentionally reports `isReady() == false` until the native library is loaded. The JNI sample is a compile-safe bridge, not a fake inference engine.

## STT

Use whisper.cpp for local speech recognition. Keep model files outside Git and load them from app-private storage.

## TTS

Piper voices can be sourced from `rhasspy/piper-voices`. Choose and redistribute a voice only when its license permits your intended distribution.

## Privacy

Do not upload screenshots, accessibility trees, voice recordings, prompts, or operator logs to a remote inference endpoint.
