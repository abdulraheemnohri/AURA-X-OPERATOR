# Local AI model setup

AURA-X Operator is designed to keep inference on-device. Model files are imported into app-private storage and are never uploaded by the application.

## Primary model

- Hugging Face repository: `Qwen/Qwen2.5-0.5B-Instruct-GGUF`
- Recommended quantization: `Q4_K_M`
- Expected filename: `qwen2.5-0.5b-instruct-q4_k_m.gguf`
- Direct asset: `https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true`
- UI: **Settings → Model Center → Local models → Download**
- Alternative: **Settings → Model Center → Hugging Face search → repository → GGUF → Download**

The application uses the downloadable asset URL, not the Hugging Face HTML repository page.

## Download lifecycle

The primary model follows:

`AVAILABLE → DOWNLOADING → READY → LOADED`

Failures enter `ERROR` and can be retried. A download interrupted after writing bytes leaves a `.part` file. On retry, AURA-X requests an HTTP Range continuation when supported. If the server rejects the old range with HTTP 416, the partial file is discarded and the download restarts cleanly.

Before a GGUF becomes `READY`, AURA-X checks:

1. File is non-empty and above the minimum GGUF size threshold.
2. GGUF magic/signature is present.
3. Expected size is checked when Hub metadata supplies it.
4. SHA-256 is checked when Hugging Face exposes an LFS hash.
5. The finalized local path exists.

## If the main model will not download

1. Open **Settings → Model Center**.
2. Check the primary model state.
3. If it is `ERROR`, press **Retry**.
4. Turn **Wi-Fi only downloads** off if mobile data is intentionally allowed.
5. Ensure sufficient free storage; model files are large.
6. Keep the app open long enough for WorkManager to start the job if the device has aggressive background restrictions.
7. If a stale interrupted download remains, retry; the downloader handles Range continuation and HTTP 416 recovery.
8. If the downloaded bytes fail validation, do not manually rename the file. Delete/retry so the integrity checks run again.

## Manual import

A compatible GGUF can be imported from local storage. A filename alone is not considered valid: the file must pass GGUF validation before it is accepted as the primary model.

Do not commit model weights to Git; they are hundreds of MB and are user data once installed.

## Native runtime

The Android build fetches pinned llama.cpp release `b10087` through CMake FetchContent and links the CPU backend into `aurax_native`.

The JNI layer:

1. Opens the imported GGUF from app-private storage.
2. Creates a 2048-token context.
3. Applies a conservative sampler.
4. Generates locally.
5. Never sends the prompt or generated text to a network service.

## Hugging Face Model Hub

Public Hub browsing does not require a token. The client supports public model search, recursive repository file listing, model metadata, GGUF/SafeTensors filtering, file size and available SHA-256 metadata, and WorkManager download queueing.

Private or gated repositories are not silently treated as public downloads. Authentication support must be added deliberately before enabling those flows.

## Vision

Vision is deliberately optional. The default operator uses AccessibilityNodeInfo text and metadata. A future Qwen-VL backend can consume screenshots only after the same sensitive/private-screen gate.

## Voice

Whisper and Piper are represented as local runtime interfaces. Native model binaries/weights are not checked into Git. They must be pinned and imported into app-private storage before enabling voice automation in a release build.

## Model provenance

The primary model is Apache-2.0 according to the official Hugging Face model card. Review the model's current license and terms before redistribution.
