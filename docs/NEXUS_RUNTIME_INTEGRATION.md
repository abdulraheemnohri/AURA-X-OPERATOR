# NEXUS Runtime Integration

The NEXUS capability catalog is now connected to conservative device-backed runtime checks.

## Availability sources

- Primary GGUF model: validated by `ModelRepository`.
- Whisper, vision and embedding models: detected only when their expected model assets exist under the app's private `files/models` directory.
- Optional wake-word, vision and memory-graph runtimes: detected only when their runtime marker assets exist under the app's private `files/runtimes` directory.
- Android permissions: checked at runtime before a permission-gated capability is exposed.

## Enforcement

Call `NexusCapabilityGate.requireAvailable()` before starting a capability from UI, automation or a plugin. This prevents a setting switch from bypassing model/runtime availability.

## Security

The provider never downloads or executes code. Runtime marker files are only availability signals; executable plugin loading remains disabled. A real runtime must still be integrated behind its own trusted interface before a capability is promoted to `READY`.

## Model/runtime asset contract

This layer intentionally does not invent model formats or native runtimes. Existing Model Hub/import flows remain the source of truth for supported assets. New native runtimes should replace marker-file detection with an explicit health probe when they are integrated.
