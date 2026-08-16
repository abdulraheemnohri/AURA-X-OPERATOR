# NEXUS Whisper Runtime Truth — 2026-08-16

## Implemented

The Whisper capability is now runtime-aware instead of treating a model file as sufficient proof of availability.

### Runtime prerequisites

`WhisperRecognizer.isRuntimeAvailable()` is true only when the packaged `aurax_whisper` JNI library loads successfully.

`WhisperRecognizer.isAvailable(modelPath)` additionally requires a real, non-empty model file.

The NEXUS availability provider therefore reports Whisper runtime availability only when both conditions are satisfied.

## Safety of the capability registry

A Whisper model sitting in app-private storage no longer implies that native inference is executable. This keeps capability reporting aligned with the project's rule that model-gated and runtime-gated features must expose truthful state.

## Remaining work

- Add stronger Whisper model-format validation/checksum metadata to the runtime availability path.
- Add instrumentation coverage for native library missing, model missing, and model present cases.
- Continue the same runtime-truth treatment for Vision, Embeddings, Piper TTS, and wake-word inference.
