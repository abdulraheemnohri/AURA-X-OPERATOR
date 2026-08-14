# A→Z Project Audit — Model Hub / Settings Hardening

## Scope

This audit covers the Android application, local model lifecycle, Hugging Face discovery, WorkManager downloads, persistence, security settings, native-runtime boundary, documentation and CI configuration.

## Verified areas

- Android 12+ configuration: `minSdk 31`, `compileSdk 34`, `targetSdk 34`.
- Java/Kotlin target: JDK 17.
- Native build: CMake + NDK 25.2.9519653, arm64-v8a.
- Hilt, Room, WorkManager and Compose are present.
- Internet permission is declared for public Hugging Face access.
- Local model storage is under the app's private files directory.
- Primary GGUF validation checks the GGUF magic, minimum size and SHA-256.
- Built-in Qwen entry uses a direct Hugging Face `resolve/main/...gguf` asset URL.
- Hugging Face search and recursive repository-file discovery are available through the public API.
- Model Hub assets are registered in Room before WorkManager download.
- WorkManager supports Wi-Fi-only constraints and resumable HTTP Range downloads.
- Downloaded assets become `READY` only after the download and optional SHA-256 verification complete.
- Loaded models must be `READY` and point to an existing local file.
- Duplicate Hugging Face client implementation was removed; `HuggingFaceClient` is the canonical Hub API client.
- Hugging Face model registry IDs are now filesystem-safe and deterministic, preventing repository paths from becoming invalid `.part` paths on Android.
- Android CI exists and verifies the declared SDK/NDK/native configuration before assembling and testing.
- Confirmation state transitions are guarded so an empty confirmation queue cannot move the operator into `EXECUTING` or `BLOCKED`.
- Confirmation waits now honor the configured confirmation window instead of blocking for the full task runtime.

## Known integration boundary

The existing Settings screen currently persists most controls immediately through `SecurePrefs`. An explicit draft/Save UX is a UI-level enhancement and should not be confused with persistence: the persistence layer is already local and encrypted for protected settings.

## Model download failure modes covered

1. Repository URL accidentally used as a file URL.
2. Resumed download receiving HTTP 200 instead of HTTP 206.
3. Interrupted `.part` files.
4. Empty/corrupt downloads.
5. SHA-256 mismatch.
6. Insufficient storage during import.
7. Model marked ready without a real local file.
8. Hugging Face repository/file paths containing `/` being used as local download identifiers.
9. Wi-Fi-only downloads running on non-Wi-Fi networks.

## Remaining validation required on a real build runner

- `assembleDebug`
- `testDebugUnitTest`
- native CMake/llama.cpp compilation
- real Hugging Face download/resume/cancel test
- load/unload of a downloaded GGUF on an arm64 Android device
- Android 12, 13 and 14 smoke tests
- accessibility, overlay and notification permission flows
- voice/STT/TTS asset compatibility

No successful CI run is claimed until GitHub Actions actually completes for the current commit.
