# AURA-X Model Hub

## Overview

The Model Hub is the local model lifecycle layer plus a public Hugging Face browser. It deliberately keeps large weights outside the APK.

### Supported lifecycle

`AVAILABLE -> DOWNLOADING -> READY -> LOADED`

Failure state: `ERROR`.

A model is not considered loadable merely because its filename exists. The local file must exist, be non-empty, and pass the model validator; SHA-256 is checked when a digest is supplied.

## Hugging Face

Public repositories can be searched without an API token. The app uses the Hugging Face Hub REST endpoints for:

- model search
- repository metadata
- recursive repository file listing
- GGUF / SafeTensors / ONNX / TFLite asset discovery
- direct asset URLs
- repository tags, downloads and likes

Authentication is intentionally not required for public model discovery. Private/gated repositories are not silently bypassed; a future authenticated flow must explicitly store credentials and respect Hugging Face access controls.

## Downloads

Downloads use WorkManager and the existing resumable HTTP pipeline. Wi-Fi-only mode maps to `NetworkType.UNMETERED`. Partial files use a `.part` suffix and HTTP Range requests when the server supports them. Completed files are renamed only after integrity validation.

## Primary model

The default planner model is:

- Repository: `Qwen/Qwen2.5-0.5B-Instruct-GGUF`
- Asset: `qwen2.5-0.5b-instruct-q4_k_m.gguf`
- Format: GGUF
- Quantization: Q4_K_M
- License: Apache-2.0

The download URL must point at the concrete `resolve/main/...` asset, not the repository landing page.

## Storage and recovery

Before installation, AURA-X checks available app-private storage. Interrupted downloads remain resumable. A failed integrity check leaves the model unusable and records `ERROR` rather than pretending that installation succeeded.

## Model actions

- Search
- Browse repository files
- Download
- Cancel
- Retry by starting the download again
- Import local model
- Integrity recheck
- Load
- Unload
- Delete user-installed model
- Select active local model through the existing settings/runtime layer

Built-in models cannot be deleted. A loaded model must be unloaded before deletion.

## Troubleshooting

### Download says HTTP error

Check connectivity and retry. A repository URL is not a file URL. Use the exact Hub asset URL.

### Download reaches 100% but model is not ready

The SHA-256 or model-format validation failed. Delete the incomplete asset and retry from the Hub file browser.

### Model downloads but cannot load

Confirm the file is GGUF for llama.cpp, the file is complete, and the device has enough RAM. Loading now requires a READY model with an actual local file.

### Wi-Fi-only does not start

The WorkManager request requires an unmetered network. Disable Wi-Fi-only when mobile data is acceptable.
