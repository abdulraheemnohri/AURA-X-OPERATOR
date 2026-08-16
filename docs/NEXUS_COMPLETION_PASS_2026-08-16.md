# AURA-X NEXUS — Completion Pass

Date: 2026-08-16
Version line: 5.0.0 NEXUS
Branch: `main`

## Purpose

This document records the implementation-hardening pass against the AURA-X NEXUS master specification. It is intentionally evidence-based: a feature is not marked READY merely because a UI, interface, or placeholder exists.

## Completed in this pass

### Model download policy

- Added a typed local `ModelDownloadSettings` policy store.
- Automatic default-model download remains enabled by default.
- Unmetered/Wi-Fi-only download is enabled by default.
- Charging-only mode is configurable.
- Low-battery protection is enforced by the download worker.
- Automatic retry can be disabled.
- Retry count is bounded from 0–10.
- Maximum parallel-download policy is stored for future queue coordination.
- Download speed-limit policy is stored for future transport throttling.
- WorkManager download constraints now derive from the explicit download policy.

### Download worker safety

- Retry behavior is now bounded by the configured retry count.
- Non-transient failures do not enter an endless retry loop.
- User-disabled automatic retry fails the work item immediately with a diagnostic payload.
- Low-battery work is deferred rather than consuming the final battery reserve.

### Vision capability truthfulness

The previous native llava boundary could report a model as loaded without a real llava.cpp runtime and could return fabricated image descriptions. That behavior violated the NEXUS capability-truthfulness rule.

The boundary now:

- refuses to report llava as loaded when the real runtime is not linked;
- returns unavailable/error state instead of fabricated vision results;
- treats a null native result as failure;
- keeps `READY` reserved for a real loaded runtime.

The CMake configuration still intentionally does not bundle llava.cpp. Therefore Vision remains `OPTIONAL_RUNTIME_GATED` until a real llava.cpp integration is added and tested.

## Current capability truth

| Capability | State | Evidence / condition |
|---|---|---|
| Local GGUF LLM | READY when validated model exists | llama.cpp JNI + GGUF validation |
| Default model auto-download | READY | WorkManager + resumable downloader |
| Model integrity verification | READY | GGUF validation + SHA-256 when supplied |
| Model cancellation | READY | WorkManager cancellation / coroutine cancellation |
| Model retry | READY | bounded WorkManager retry policy |
| Accessibility operator | PERMISSION_GATED | Android AccessibilityService must be enabled |
| Operator abort | READY/PERMISSION_GATED by path | receiver + runtime safety controller |
| Safety confirmation | READY | policy/confirmation layer |
| Vision | OPTIONAL_RUNTIME_GATED | real llava.cpp runtime is not bundled |
| Whisper STT | MODEL_GATED | native whisper runtime + model required |
| Android TTS | READY where Android TTS engine exists | platform runtime |
| Piper TTS | OPTIONAL_RUNTIME_GATED | native Piper runtime/model required |
| Wake word | OPTIONAL_RUNTIME_GATED | wake-word engine/model required |
| RAG/vector memory | MODEL/ENGINE-GATED as configured | local embedding/runtime availability |
| LAN server | OPTIONAL | explicit user configuration required |
| Plugins | DISABLED by default | trusted registry + explicit policy |

## Remaining implementation gates

These are engineering tasks, not UI placeholders to be marked complete without evidence.

1. Integrate a real llava.cpp Android-compatible runtime and model lifecycle.
2. Integrate and test a real local Piper runtime if Piper is selected as an installed TTS engine.
3. Integrate a real wake-word engine/model and measure idle battery impact.
4. Complete streaming token generation if the runtime abstraction requires true token-level streaming.
5. Complete end-to-end Observe → Plan → Act → Verify → Recover → Replan tests on a physical Android device.
6. Add explicit battery-percentage scheduling rather than relying only on WorkManager's coarse `BatteryNotLow` constraint for all queued work.
7. Apply the stored download speed-limit policy to the HTTP transport.
8. Enforce the stored maximum-parallel-download policy in the model queue coordinator.
9. Migrate legacy settings from `SharedPreferences` to the final DataStore-backed settings architecture without breaking existing installations.
10. Add release-build signing/configuration verification in CI.
11. Run the final API 28–35 compatibility matrix on supported emulator/device images.
12. Complete the final security review and physical-device automation test suite.

## Release rule

AURA-X must not claim NEXUS 5.0 production-complete status until the remaining gates above are either implemented and verified or explicitly moved to a documented post-5.0 release scope.

## Engineering principle

The repository is upgraded in place. Existing working subsystems are preserved, duplicate implementations are consolidated, and capability status is derived from real runtime state rather than hardcoded UI claims.
