# README Upgrade Notes

This file is the source-of-truth supplement for the current Model Hub and Settings hardening branch.

## Model Hub

AURA-X can discover public Hugging Face models, inspect repository files, select a downloadable asset, register it in the local Room registry and queue it through WorkManager. The existing download pipeline supports resumable HTTP downloads, Wi-Fi-only constraints and integrity verification.

For GGUF models, select the actual `.gguf` asset from the repository. A repository landing page is not a model file.

## Local-first guarantees

- Models are stored in app-private storage.
- No model weights are bundled into the Git repository.
- Public Hugging Face discovery does not require a token.
- Optional/private Hub access must be added later without committing credentials.
- Safety and operator settings remain local.

## Settings

The Settings surface includes automation policy, safety limits, biometric protection, incognito protection, visible operator status, voice controls, inference parameters, local model selection, appearance, permissions, diagnostics and local safety-log export.

The current persistence contract uses local preferences; safety-sensitive settings are stored with encrypted preferences. The UI has been audited so capability status is presented as READY, PERMISSION, MODEL or OPTIONAL instead of pretending optional runtime assets are installed.

## Build and release checklist

```text
./gradlew assembleDebug
./gradlew test
```

Before a release, also verify:

- native CMake build succeeds
- arm64 APK installs on Android 9+
- primary GGUF can be downloaded from Hugging Face
- interrupted download resumes
- SHA-256 is enforced when provided
- model cannot enter READY without a local file
- model can load/unload successfully
- emergency abort remains available
- accessibility/overlay/notification permissions are handled explicitly

## Documentation map

- `docs/ARCHITECTURE.md`
- `docs/MODEL_HUB.md`
- `docs/SETTINGS.md`
- `docs/A_TO_Z_AUDIT.md`
- `docs/CHANGELOG_MODEL_HUB.md`
- `MODEL_SETUP.md`
- `SECURITY.md`
