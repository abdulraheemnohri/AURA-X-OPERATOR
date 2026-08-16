# AURA-X NEXUS — Vision Runtime Truth

Date: 2026-08-16

## Implemented

The NEXUS runtime availability provider no longer treats the presence of a vision model file as proof that vision is executable.

Vision is exposed as an installed runtime only when both conditions are true:

1. `vision-model.bin` exists and is non-empty.
2. The `aurax_llava` native JNI library successfully loads.

`LlavaVisionRuntime.isNativeRuntimeAvailable()` exposes the native-library state without loading a model or claiming inference readiness.

## Important limitation

The repository's current `llava-native.cpp` is still a conservative JNI boundary and deliberately refuses to load a model until a real llava.cpp runtime is linked. Therefore a vision model file alone will remain gated and will not become `READY`.

This is intentional: AURA-X must never advertise vision capability or act on fabricated visual understanding.

## Next vision gate

Link and validate the actual llava.cpp runtime, then add an inference health check before exposing vision as fully operational.
