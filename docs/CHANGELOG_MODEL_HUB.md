# Model Hub Upgrade Changelog

## 2026-08-14

### Fixed

- Corrected the built-in Qwen GGUF source from the Hugging Face repository landing page to the concrete downloadable Q4_K_M asset.
- Hardened model loading so READY must correspond to an actual local model file.
- Added public Hugging Face Hub REST discovery without adding a third-party HTTP dependency.

### Added

- Hugging Face model search client
- Repository file listing
- Direct Hub asset URL resolution
- Hub model registration into the local Room model registry
- GGUF/SafeTensors/ONNX/TFLite asset classification
- Wi-Fi-only download support through the existing WorkManager pipeline
- Model Hub documentation
- Settings persistence/save contract documentation
- Architecture documentation

### Validation requirements

A successful HTTP response is not sufficient. The download pipeline must validate the resulting local file and, when a digest is available, verify SHA-256 before changing the model state to READY.

### Remaining integration work

The existing Compose Model Center still needs the final UI wiring for the new Hub browser and an explicit Save Settings button if a future branch modifies the screen-level draft behavior. The repository write API blocked those replacement-file operations during this pass, so this changelog intentionally does not claim that those two UI integrations are complete.
