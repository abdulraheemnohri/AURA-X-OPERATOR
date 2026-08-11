# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

A local-first Android automation assistant for personal sideloading. It uses Android AccessibilityService only after explicit user enablement and keeps sensitive interaction behind hard guardrails.

## Safety model

- Password/credential entry is never automated.
- Sensitive screens are blocked when detected.
- Banking/authenticator/settings-security packages are blocked.
- Volume Down aborts the operator immediately.
- The floating indicator is visible while the overlay service is active.
- The notification includes a **Stop AURA-X Operator** action.
- Private/incognito browsing is detected from visible accessibility text and is not automated.
- Operator actions and safety events are persisted locally with Room.
- No model weights are committed to this repository.

## Build

Open the project in Android Studio Hedgehog or newer with Android SDK 34, NDK 25.2, CMake 3.22+, Kotlin 1.9.x, and JDK 17. The repository intentionally does not include large model weights or third-party native model sources.

## Models

See [MODEL_SETUP.md](MODEL_SETUP.md) for the Hugging Face model sources and local integration points.

## Scope

This repository is intended for personal/sideloaded use. It is not designed to conceal automation, bypass authentication, automate payments, or interact with passwords/OTPs.

## Project structure

- `operator/` — AccessibilityService, screen extraction, guardrails, abort handling
- `agent/` — planning, risk analysis, verification
- `tools/` — safe Chrome/YouTube/system integrations
- `data/` — Room schema and audit records
- `ui/` — Compose dashboard, chat, operator, tasks, and settings
- `ai/` — model runtime abstraction and JNI bridge
- `voice/` — local STT/TTS integration points

## Donation

If this project is useful, support future maintenance through your preferred donation channel.
