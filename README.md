# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

AURA-X Operator is a sideloaded, local-first Android automation assistant built around AccessibilityService, Jetpack Compose, Room and a native llama.cpp bridge.

## Current Android target

- **Minimum:** Android 12 / API 31
- **Target/Compile:** Android 14 / API 34
- **ABI:** arm64-v8a debug APK
- **Java:** 17
- **Native toolchain:** CMake 3.31.6 / NDK 25.2.9519653
- **Distribution:** direct APK / sideload only

## Implemented capabilities

- Accessibility UI inspection with text/content-description/resource-id/class matching
- Guarded click, type, scroll and navigation operations
- Sensitive/password/private-browsing detection
- Blocked security/payment/authentication package protection
- Automation policies: Observe Only, Suggest Only, Confirm Actions, Full Auto Low Risk
- Configurable confirmation countdown for policy-controlled actions
- Emergency abort through Volume Down, notification action and floating overlay
- Persistent operator status indicator
- Chrome safe automation and sensitive-flow blocking
- YouTube safe automation with likes/subscriptions/comments/ads blocked
- Installed-app opening and supported Android settings navigation
- Screenshot capture and accessibility-tree screen context
- Task planner, executor and post-action verification hooks
- Room persistence for conversations, messages, memories, tasks, actions and safety events
- Local CSV safety-log export
- Biometric operator unlock
- GGUF model import and llama.cpp JNI runtime integration
- Voice UI and Android TTS compatibility fallback
- Whisper/Piper native integration points that require compatible local model/runtime assets
- Persistent onboarding and startup routing
- Runtime model presets, performance and thermal controls
- Analytics and safety-event reporting
- Semantic local memory retrieval infrastructure
- Guarded tool registry and operator risk classification
- Android 31+ CI verification and debug APK artifact publishing

## Model Hub

The Model Center now provides a local-first Hugging Face workflow for public repositories:

- Search public Hugging Face models
- Browse repository files recursively
- Filter repository files by GGUF, SafeTensors or all files
- Show model downloads, likes, tags and pipeline metadata
- Show file size and available SHA-256/LFS hash
- Queue downloads through WorkManager
- Wi-Fi-only download mode
- Resumable `.part` downloads when the server supports HTTP Range
- HTTP 416 recovery by restarting a stale partial download
- Safe filesystem model names and stable Hub registry IDs
- Expected-size validation when metadata is available
- SHA-256 verification when the Hub exposes a hash
- GGUF magic/signature and minimum-size validation before `READY`
- Direct download/retry for the built-in primary Qwen model
- Download progress and lifecycle state in the local model list
- Load, unload, cancel and delete model lifecycle controls
- Persistent Room model metadata and local paths
- Refusal to load a model whose local file is missing

### Primary model

The built-in primary planner is:

**Qwen 2.5 0.5B Instruct — GGUF Q4_K_M**

The application uses the actual Hugging Face downloadable asset rather than the repository HTML page:

`Qwen/Qwen2.5-0.5B-Instruct-GGUF/qwen2.5-0.5b-instruct-q4_k_m.gguf`

The APK does **not** bundle the model weights. Open **Settings → Model Center**, then use **Download** for the primary model or browse Hugging Face and select another compatible asset.

## Settings

Settings are persisted locally through the protected preferences layer and now expose an explicit staged workflow:

- **Save Settings** commits the current staged values
- **Discard** reloads the last persisted values
- Unsaved-change state is shown before leaving the main settings surface
- Automation policy and safety controls
- Biometric unlock and operator indicator
- Incognito protection and haptics
- Confirmation countdown
- Maximum actions per task
- Maximum task runtime
- Appearance mode
- Local model temperature
- Maximum output tokens
- Context tokens
- STT language
- Voice, privacy, safety and diagnostics centers
- Permission readiness checks
- Safety-log export

## Safety model

AURA-X is deliberately transparent: automation is visible, abortable and audited. Password-like fields, authentication/payment/security flows and private browsing are never automated. The operator does not silently bypass Android permissions.

## Local model assets

The application does not bundle large AI model weights into the APK. Import a compatible GGUF model through Settings or download one through Model Center. Whisper/Piper model assets are similarly expected in app-private storage when those native backends are enabled.

A filename alone is never treated as proof that a model works. GGUF assets are validated before they enter the `READY` state.

## Build

Use Android Studio Hedgehog or newer with JDK 17. The repository includes an Android CI workflow that verifies API 34 compilation, API 31 minimum configuration, arm64-v8a native packaging, unit tests and the debug APK artifact.

```text
gradle --no-daemon assembleDebug
gradle --no-daemon testDebugUnitTest
```

## Documentation

- `MODEL_SETUP.md` — local model setup and troubleshooting
- `docs/MODEL_HUB.md` — Hugging Face and Model Hub workflow
- `docs/SETTINGS.md` — settings and persistence
- `docs/ARCHITECTURE.md` — system architecture
- `docs/A_TO_Z_AUDIT.md` — project audit checklist
- `docs/RELEASE_GATE.md` — pre-release verification gate
- `docs/CHANGELOG_AZ_HARDENING.md` — hardening changes

## APK

GitHub Actions → latest **Android CI** run → **Artifacts** → `aura-x-operator-android31plus-debug`.

Install the APK on an Android 12+ arm64 device, then enable AccessibilityService and overlay access from Settings before running operator actions.

## Important limitation

Native Whisper/Piper and multimodal vision require their actual model/runtime assets. The Kotlin interfaces and safety plumbing are present, but the repository does not pretend that an absent model file is a working inference backend.
