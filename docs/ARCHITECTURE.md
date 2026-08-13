# AURA-X Architecture Guide

## Runtime layers

- UI: Jetpack Compose screens and view models
- Operator: accessibility, overlay, notifications and guarded action execution
- AI: model registry, Hub discovery, download lifecycle and llama.cpp runtime
- Data: Room entities/DAO plus encrypted secure preferences
- Safety: risk classification, confirmation gates, abort handling and sensitive-flow blocking
- Native: llama.cpp plus optional Whisper/Piper integration points

## Model flow

`Hugging Face REST -> Model Hub registry -> WorkManager -> resumable file -> integrity validation -> READY -> llama.cpp load`

A model cannot move to a loadable state simply because a network request completed. The file and format must be validated.

## Settings flow

`Compose draft -> Save Settings -> persisted local store -> runtime re-read/apply`

Security-sensitive controls use encrypted preferences. General runtime settings use the typed settings repository.

## Failure philosophy

AURA-X prefers explicit failure over false readiness. Network failures, missing assets, invalid hashes, missing permissions and unsupported model formats should surface as actionable errors.

## Security boundary

Accessibility automation is constrained by the safety engine. Authentication, password, payment and private browsing flows remain blocked. Android permissions are never silently bypassed.
