# AURA-X Stabilization Pass

This pass focuses on cross-layer correctness rather than adding another feature layer.

## Fixed

- Native llama.cpp model memory is now explicitly released when the active model is unloaded.
- Switching from one active model to another releases the previous native model before changing persisted lifecycle state.
- The Kotlin runtime exposes a native unload bridge to the JNI layer.
- Settings saved from the Settings UI are mirrored into the runtime `SettingsRepository` for overlapping values, preventing the UI and inference engine from silently using different values.
- Policy, biometric lock, incognito protection, confirmation timeout, task limits, theme, temperature, max output tokens, context length, and STT language now synchronize between the encrypted UI settings store and runtime settings.

## Remaining release gates

1. Run the full Android debug build and unit-test workflow.
2. Verify JNI symbol resolution for `nativeUnload` on arm64-v8a.
3. Install on an Android 9+ arm64 device and load/unload the same GGUF repeatedly.
4. Switch between two GGUF models and verify native memory is released between switches.
5. Change inference settings in Settings, press Save Settings, restart the app, and verify generated output uses the saved runtime values.
6. Verify Hugging Face download, resume, checksum validation, READY transition, load, unload, and delete.
7. Verify Accessibility, overlay, microphone, and notification permission flows.

## Architectural rule

`SecurePrefs` remains the encrypted UI/policy persistence layer. `SettingsRepository` remains the runtime configuration surface. Overlapping values must be synchronized whenever SecurePrefs is committed from the Settings UI. New runtime settings should be added to one canonical contract rather than creating another independent store.
