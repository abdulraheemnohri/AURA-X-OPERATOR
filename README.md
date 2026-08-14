# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

AURA-X Operator is a sideloaded, local-first Android automation assistant built around AccessibilityService, Jetpack Compose, Room, WorkManager and a native llama.cpp bridge.

## Current Android target

- **Minimum:** Android 9 / API 28
- **Target/Compile:** Android 14 / API 34
- **ABI:** arm64-v8a
- **Java:** 17
- **Native toolchain:** CMake 3.31.6 / NDK 25.2.9519653
- **Version:** 4.0.0 NEXUS
- **Distribution:** direct APK / sideload only

## Implemented capabilities

- Accessibility UI inspection with text/content-description/resource-id/class matching
- Guarded click, type, scroll and navigation operations
- Sensitive/password/private-browsing detection
- Blocked security/payment/authentication package protection
- Automation policies: Observe Only, Suggest Only, Confirm Actions, Full Auto Low Risk
- Configurable confirmation countdown, task limits and runtime limits
- Emergency abort through Volume Down, notification action, floating overlay, Quick Settings and home-screen widget
- Persistent operator status indicator
- Chrome safe automation and sensitive-flow blocking
- YouTube safe automation with likes/subscriptions/comments/ads blocked
- Installed-app opening and supported Android settings navigation
- Screenshot capture and accessibility-tree screen context
- Task planner, executor, post-action verification and recovery hooks
- Room persistence for conversations, messages, memories, tasks, actions and safety events
- Local CSV safety-log export
- Biometric operator unlock
- GGUF model import and llama.cpp JNI runtime integration
- Hugging Face Model Hub search, recursive file browsing, resumable downloads, integrity verification and lifecycle management
- Local Whisper bridge with Android SpeechRecognizer fallback
- Local Android TTS compatibility backend with optional native Piper integration
- Wake-word/continuous-conversation state gate with explicit model/runtime gating
- Local file-backed RAG with deterministic embeddings and top-K retrieval
- Encrypted local backup export and non-destructive restore/merge
- Room-backed operational analytics
- Authenticated loopback-only companion endpoint
- Trusted in-process plugin SDK and deterministic plugin registry; arbitrary downloaded code is disabled
- Persistent onboarding and startup routing
- Runtime model presets, performance and thermal controls
- Granular local settings with Save/Discard workflow
- Android Quick Settings tile and home-screen operator cockpit

## Model Hub

The Model Center provides a local-first Hugging Face workflow for public repositories:

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
- Load, unload, cancel and delete model lifecycle controls
- Persistent Room model metadata and local paths
- Refusal to load a model whose local file is missing

### Primary model

**Qwen 2.5 0.5B Instruct — GGUF Q4_K_M**

`Qwen/Qwen2.5-0.5B-Instruct-GGUF/qwen2.5-0.5b-instruct-q4_k_m.gguf`

The APK does not bundle model weights. Open **Settings → Model Center** and download/import a compatible model.

## Settings

Settings are persisted locally through the protected preferences layer and expose an explicit staged workflow:

- Save Settings / Discard
- Automation policy, confirmation window and task limits
- Biometric lock, operator indicator, incognito protection and password filtering
- Screenshot verification and screen-change abort
- Model, context, temperature, top-p, top-k, output tokens, CPU threads, GPU acceleration and batch size
- Wake-word enablement, sensitivity and continuous-conversation timeout
- STT/TTS model and language controls
- Memory retention, vector search, RAG and top-K retrieval controls
- Local-only mode and optional search/network controls
- Theme, dynamic colors, animations, reduced motion, font scale and orb style
- Performance mode, battery threshold, thermal protection and automatic model unload
- Loopback companion port/authentication and remote-model URL
- Trusted plugin execution gate (disabled by default)
- Developer/debug/verbose logging controls
- Safety-log export, diagnostics and permission readiness

## Safety model

AURA-X is deliberately transparent: automation is visible, abortable and audited. Password-like fields, authentication/payment/security flows and private browsing are never automated. The operator does not silently bypass Android permissions.

## Known gated capabilities

Some capabilities are intentionally not claimed as fully bundled:

- Real microphone wake-word inference still needs a compatible detector model/runtime.
- Multimodal vision interpretation needs a compatible local vision runtime/model.
- Direct SafeTensors execution is not bundled.
- On-device model conversion/quantization is not bundled.
- Arbitrary third-party downloaded plugin code is not executed.

These gates are deliberate safety and runtime-integrity boundaries, not fake feature claims.

## Build

Use Android Studio Hedgehog or newer with JDK 17.

```text
gradle --no-daemon assembleDebug
gradle --no-daemon testDebugUnitTest
```

The CI workflow is the source of truth for build verification. A source review is not treated as proof of a successful native Android build.

## Documentation

- `MODEL_SETUP.md` — local model setup and troubleshooting
- `docs/MODEL_HUB.md` — Hugging Face and Model Hub workflow
- `docs/SETTINGS.md` — settings and persistence
- `docs/ARCHITECTURE.md` — system architecture
- `docs/A_TO_Z_AUDIT.md` — project audit checklist
- `docs/RELEASE_GATE.md` — pre-release verification gate
- `docs/NEXUS_UPGRADE_2026-08-14.md` — NEXUS upgrade notes

## APK

GitHub Actions → latest **Android CI** run → **Artifacts** → `aura-x-operator-android31plus-debug` when available.

Install the APK on an Android 9+ arm64 device, then enable AccessibilityService and overlay access from Settings before running operator actions.
