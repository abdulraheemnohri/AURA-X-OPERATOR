# NEXUS implementation status

This document is the release contract for the current NEXUS implementation.

## Implemented in the application

- Closed-loop operator lifecycle and safety-controller abort path
- Countdown/confirmation and post-action verification state
- Local persistent state and staged runtime settings
- Local RAG integration surface
- Analytics dashboard and task aggregation
- Encrypted backup/restore surface
- Loopback companion surface
- Trusted in-process plugin SDK and deterministic registry
- Android home-screen operator cockpit
- Model Hub management surface
- Centralized capability registry
- Centralized settings validation and sanitization
- Android 9 / API 28 minimum compatibility target

## Runtime-gated capabilities

The following capabilities are represented by real contracts and explicit gates but are not falsely marked READY until their runtime/model asset exists:

- wake-word inference
- Whisper model execution
- image/OCR runtime
- local vision model execution
- embedding model execution
- memory graph runtime
- SafeTensors inference backend
- model conversion toolchain

## Security rule

Arbitrary downloaded APK/Dex/plugin code execution is disabled. A plugin can only execute through the trusted in-process SDK contract and the existing safety/permission controls.

## Release rule

A capability may only be shown as executable when its `CapabilityState` and `NexusRuntimeAvailability` agree. UI labels must not be used to bypass this gate.

## CI contract

The Android workflow validates compile SDK 34, minimum SDK 28, target SDK 34, NDK 25.2, arm64-v8a, the native source surface, debug APK assembly, APK output and unit tests. The workflow is also manually dispatchable for release validation.
