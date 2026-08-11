# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

A sideloaded, local-first Android automation assistant built around AccessibilityService, deterministic safety policies, Room audit logs, Compose UI, and a native llama.cpp runtime.

## Current implementation

- Android 9+ / API 28 minimum, API 34 target
- Kotlin + Jetpack Compose + Material 3
- AccessibilityService with system-wide observation
- Password, sensitive-screen, blocked-package, and private-browsing gates
- Three-second visible click countdown
- Persistent floating status indicator
- Tap indicator or press Volume Down to abort
- Ongoing notification with **Stop AURA-X Operator**
- Chrome safe subset: HTTPS search/open flow with sensitive-page blocking
- YouTube safe subset: search/playback/volume; no likes, subscriptions, comments, or ad interaction
- Deterministic task planner and policy-aware executor
- Room conversations, messages, memories, tasks, operator actions, and safety events
- Local screenshot capture with sensitive/private screen blocking on Android 11+
- Biometric unlock entry point for Operator mode
- Local GGUF import and llama.cpp JNI inference
- GitHub Actions Android build + unit-test workflow

## Safety model

`OBSERVE_ONLY` and `SUGGEST_ONLY` never execute automation. `CONFIRM_ACTIONS` is the recommended default. `FULL_AUTO_LOW_RISK` is limited by tool risk and the same screen-level guardrails; the operator still uses the visible action gate for clicks.

The app intentionally does not automate password entry, financial/payment flows, OTP/security codes, private browsing, YouTube engagement, comments, or ads.

## Build

Open the repository in Android Studio Hedgehog or newer with Android SDK 34, NDK 25.2.9519653, and CMake 3.22.1.

The native build fetches pinned llama.cpp release `b10087`, so the first native build requires network access. Runtime inference is local after the model is imported.

## Model

Import `qwen2.5-0.5b-instruct-q4_k_m.gguf` from the official `Qwen/Qwen2.5-0.5B-Instruct-GGUF` Hugging Face repository through **Settings → Import GGUF model**. Model weights are intentionally excluded from Git.

## Distribution

This project is designed for personal sideloading. It is not positioned as a covert accessibility automation service or a Play Store accessibility utility.
