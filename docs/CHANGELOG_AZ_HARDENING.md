# A→Z Hardening Changelog

## 2026-08-14

### Fixed

- Fixed the built-in Qwen download URL so it resolves to the actual GGUF asset.
- Removed the duplicate `HuggingFaceModelClient` abstraction.
- Made Hugging Face registry IDs deterministic and filesystem-safe.
- Prevented repository paths from being reused directly as `.part` filenames.
- Sanitized imported model filenames before storing them locally.
- Kept `READY` dependent on a real local file.
- Kept model loading dependent on `READY` plus local-file existence.

### Added

- Public Hugging Face model search.
- Recursive repository-file discovery.
- Remote model metadata and file metadata models.
- Hub asset registration in Room.
- WorkManager download queue integration.
- Wi-Fi-only download constraints.
- A→Z audit documentation.
- README upgrade notes and release validation checklist.

### Validation policy

The project does not claim a clean build merely because source edits were made. A release is considered verified only after Android CI or a real Gradle/native build completes successfully.
