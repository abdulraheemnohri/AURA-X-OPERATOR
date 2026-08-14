# AURA-X NEXUS v3 implementation status

This document tracks the v2/v3 upgrade scope against the current runtime instead of presenting planned features as already implemented.

## Implemented and integrated

- Android 9+ baseline with Kotlin/Compose/Hilt architecture.
- Accessibility-based guarded operator execution.
- Deterministic safety policy and emergency abort.
- Local GGUF inference through llama.cpp JNI.
- Hugging Face public search and repository file browsing.
- Model registration and local Room-backed lifecycle.
- Resumable model downloads with `.part` files.
- HTTP 416 recovery.
- HTTP 206 `Content-Range` validation.
- Expected-size and SHA-256 validation when metadata is available.
- GGUF signature validation.
- WorkManager download queue.
- Idempotent download enqueueing with `KEEP`.
- Retry for transient network failures.
- Cancel and retry UI states.
- Native model unload lifecycle.
- Explicit Settings Save/Discard workflow.
- Permission Center, Safety Center, Privacy Center, Voice Center and Diagnostics screens.
- Nexus v3 capability dashboard.

## Partially implemented / model-gated

- Advanced voice: local STT/TTS architecture exists, but wake-word and continuous conversation require dedicated model/runtime integration.
- Vision: accessibility/screenshot context exists; multimodal interpretation requires a compatible vision model.
- Memory/RAG: local memory and persistence exist; a full vector index/retrieval pipeline is not yet the default runtime.
- Analytics: diagnostics and local operational data exist; the full benchmark/analytics dashboard needs its complete data pipeline.
- Backup/restore: local export paths exist; a complete encrypted application backup/restore workflow remains a release-gated feature.

## Not bundled by default

- Arbitrary third-party plugin execution.
- Unrestricted LAN agent server.
- Home-screen widgets.
- On-device Safetensors Transformers runtime.
- Full model conversion/quantization pipeline.

These are intentionally not represented as READY capabilities until their security, lifecycle, and runtime paths are implemented and tested.

## Model Hub release gate

A model is only considered usable after:

1. Download is queued by WorkManager.
2. Network and storage requirements are satisfied.
3. Resume state is validated when a partial file exists.
4. Final size is validated when expected size is known.
5. SHA-256 is validated when pinned/provided.
6. GGUF signature is validated for GGUF models.
7. The file is atomically promoted from `.part` to the final model path.
8. Device/model compatibility is checked before load.
9. Native runtime load succeeds.
10. Native unload releases the model allocation.

## Single-branch policy

All current stabilization and feature work is intentionally continued on the existing `fix/model-hub-download-ci` branch until the release gate passes. Do not create parallel feature branches for this work.
