# NEXUS Runtime Truth Pass — 2026-08-16

## Scope

This pass hardens the NEXUS capability registry so local llama.cpp inference is not advertised as available merely because a GGUF file exists.

## Implemented

- `LlamaCppRuntime.isNativeRuntimeAvailable()` exposes whether the packaged `aurax_native` JNI library loaded successfully.
- `LlamaCppRuntime.isOperational()` requires both the native runtime and a validated primary GGUF model.
- `LlamaCppRuntime.isReady()` now delegates to the same operational truth.
- `NexusRuntimeAvailabilityProvider` uses the runtime's operational state for the `llama.cpp + GGUF` capability requirement.
- A missing JNI library, invalid/missing GGUF file, or failed native load therefore keeps the capability gated.

## Safety rule

`models.llama_cpp` remains `MODEL_GATED` in the static registry. The runtime availability snapshot determines whether its actual prerequisite is satisfied.

A model file existing on disk is not sufficient evidence that inference can execute.

## Remaining runtime work

- Add a lightweight native health probe that validates JNI entry points without running a full user generation.
- Add instrumentation coverage for native library missing/present states.
- Surface the runtime failure reason in Diagnostics and Model Hub.
- Apply the same truthful-runtime pattern to Whisper, vision, embeddings, TTS, and wake-word capabilities.

## Verification status

Code has been pushed to `main`. GitHub Actions must still compile and test the new changes before this pass can be considered CI-green.
