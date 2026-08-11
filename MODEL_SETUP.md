# Local AI model setup

AURA-X Operator is designed to keep inference on-device. Model files are imported into app-private storage and are never uploaded by the application.

## Primary model

- Hugging Face repository: `Qwen/Qwen2.5-0.5B-Instruct-GGUF`
- Recommended quantization: `Q4_K_M`
- Expected filename: `qwen2.5-0.5b-instruct-q4_k_m.gguf`
- Import path: **Settings → Import GGUF model**

The official Qwen model card documents GGUF variants and llama.cpp usage. Do not commit the model weights to Git; they are hundreds of MB and are user data once installed.

## Native runtime

The Android build fetches pinned llama.cpp release `b10087` through CMake FetchContent and links the CPU backend into `aurax_native`.

The JNI layer:

1. Opens the imported GGUF from app-private storage.
2. Creates a 2048-token context.
3. Applies a conservative sampler.
4. Generates locally.
5. Never sends the prompt or generated text to a network service.

## Vision

Vision is deliberately optional. The default operator uses AccessibilityNodeInfo text and metadata. A future Qwen-VL backend can consume screenshots only after the same sensitive/private-screen gate.

## Voice

Whisper and Piper are represented as local runtime interfaces. Native model binaries/weights are not checked into Git. They must be pinned and imported into app-private storage before enabling voice automation in a release build.

## Model provenance

The primary model is Apache-2.0 according to the official Hugging Face model card. Review the model's current license and terms before redistribution.
