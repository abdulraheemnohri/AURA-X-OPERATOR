# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

AURA-X Operator is a sideloaded, local-first Android automation assistant built around AccessibilityService, Jetpack Compose, Room and a native llama.cpp bridge.

## Current Android target

- **Minimum:** Android 9 / API 28
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
- Three-second confirmation countdown for policy-controlled actions
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
- Android 9+ CI verification and debug APK artifact publishing

## Safety model

AURA-X is deliberately transparent: automation is visible, abortable and audited. Password-like fields, authentication/payment/security flows and private browsing are never automated. The operator does not silently bypass Android permissions.

## Local model assets

The application does not bundle large AI model weights into the APK. Import a compatible GGUF model through Settings. Whisper/Piper model assets are similarly expected in app-private storage when those native backends are enabled.

## Build

Use Android Studio Hedgehog or newer with JDK 17. The repository includes an Android CI workflow that verifies API 34 compilation, API 28 minimum configuration, arm64-v8a native packaging, unit tests and the debug APK artifact.

## APK

GitHub Actions → latest **Android CI** run → **Artifacts** → `aura-x-operator-android9-debug`.

Install the APK on an Android 9+ arm64 device, then enable AccessibilityService and overlay access from Settings before running operator actions.

## Important limitation

Native Whisper/Piper and multimodal vision require their actual model/runtime assets. The Kotlin interfaces and safety plumbing are present, but the repository does not pretend that an absent model file is a working inference backend.
