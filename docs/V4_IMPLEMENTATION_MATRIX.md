# AURA-X NEXUS v4 implementation matrix

This matrix reconciles the supplied v2.0, Sideloaded Edition, v3.0 MEGA and MASTER v4.0 specifications with the current repository. A feature is only called **READY** when there is a concrete runtime path; model-dependent features remain explicitly gated.

## READY in the current branch

- Android API 28 minimum, API 34 compile/target baseline.
- Hilt application and modules.
- Guarded AccessibilityService operator.
- Chrome and YouTube safe-subset tools.
- Deterministic risk/policy checks and emergency abort.
- Persistent operator overlay, notification abort, Quick Settings abort and home-screen cockpit widget.
- Local GGUF model lifecycle, resumable downloads, verification and llama.cpp JNI runtime.
- Whisper JNI bridge and voice state management.
- Room-backed conversations, tasks, memories, models and safety events with migrations.
- Memory retrieval before planning and deterministic memory extraction after execution.
- Local file-backed RAG/vector retrieval path.
- Encrypted local backup/export primitives.
- Loopback-only companion path with authentication controls.
- Battery/thermal-aware runtime controls.
- Settings covering AI, voice, operator, memory/RAG, privacy, appearance, performance, network and developer controls.
- Analytics aggregation and an interactive analytics dashboard.
- Trusted in-process plugin contract and deterministic registry.

## Explicitly model/runtime gated

- Wake-word inference: configuration/state machine exists; a dedicated detector model/runtime is required for always-listening inference.
- Continuous local conversation: lifecycle exists; continuous microphone inference remains gated by the STT runtime/model and Android permission.
- Vision interpretation: screenshot/accessibility perception exists; multimodal reasoning requires a compatible vision runtime/model.
- Document understanding: ingestion can be local; semantic/multimodal extraction requires the configured model/runtime.
- Native Piper voice: Android TTS is available as fallback; Piper requires a compatible voice runtime/model.

## Not bundled intentionally

- Arbitrary downloaded APK/Dex plugin execution.
- Unrestricted remote/LAN agent execution.
- On-device SafeTensors Transformers runtime.
- On-device model conversion/quantization pipeline.

## Release gate

1. GitHub Actions build succeeds.
2. Unit tests succeed.
3. Native llama.cpp/Whisper compilation succeeds.
4. API 28 compatibility is verified on a real Android 9 device/emulator.
5. Accessibility, abort, confirmation and protected-action tests pass.
6. Model download/verification/load/unload lifecycle passes.
7. Backup/restore is verified with real data.
8. No UI reports model-gated functionality as READY.
9. Release APK is generated and manually smoke-tested.
