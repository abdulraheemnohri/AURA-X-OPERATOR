# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

AURA-X Operator is a sideloaded, local-first Android automation assistant built around AccessibilityService, Jetpack Compose, Room and a native llama.cpp bridge.

## Current Android target

- **Minimum:** Android 13 / API 33
- **Target/Compile:** Android 14 / API 34
- **ABI:** arm64-v8a debug APK
- **Distribution:** direct APK / sideload only

The repository intentionally targets Android 13+; older Android releases are not supported by the current build.

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
- Android 13+ CI verification and debug APK artifact publishing

## Safety model

AURA-X is deliberately transparent: automation is visible, abortable and audited. Password-like fields, authentication/payment/security flows and private browsing are never automated. The operator does not silently bypass Android permissions.

## Local model assets

The application does not bundle large AI model weights into the APK. Import a compatible GGUF model through Settings. Whisper/Piper model assets are similarly expected in app-private storage when those native backends are enabled.

## Build

Use Android Studio Hedgehog or newer with JDK 17. The CI workflow assembles the Android 13+ debug APK and uploads it as a workflow artifact.

## APK

GitHub Actions → latest **Android CI** run → **Artifacts** → `aura-x-operator-android13-debug`.

Install the APK on an Android 13+ arm64 device, then enable AccessibilityService and overlay access from Settings before running operator actions.

## Important limitation

Native Whisper/Piper and multimodal vision require their actual model/runtime assets. The Kotlin interfaces and safety plumbing are present, but the repository does not pretend that an absent model file is a working inference backend.
