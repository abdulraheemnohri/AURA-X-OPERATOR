# AURA-X NEXUS — Next Engineering Pass

Date: 2026-08-16

## Objective

Continue the existing AURA-X Operator repository in place. This pass prioritizes truthful runtime capability reporting, settings migration, model scheduling policy, and closed-loop operator foundations. No feature is marked READY unless the underlying implementation is available.

## Workstream

1. Model download queue policy
2. DataStore-backed settings migration
3. Capability registry truthfulness
4. Runtime availability diagnostics
5. Operator state/policy integration
6. Verification and recovery integration
7. Tests and CI
8. Documentation synchronization

## Capability status contract

- READY: runtime is installed and operational.
- PERMISSION_GATED: implementation exists but Android permission/service enablement is required.
- MODEL_GATED: implementation exists but a compatible model is not installed.
- OPTIONAL_RUNTIME_GATED: implementation depends on an optional native/runtime component.
- NOT_BUNDLED: feature is intentionally not shipped in the APK.
- DISABLED: user or policy disabled the capability.
- ERROR: capability exists but its runtime currently reports an error.

## Model download requirements

The model downloader must honor configured network, charging, battery, retry, speed, and concurrency policies. A valid installed model must never be replaced by an unverified download. Downloads must remain resumable and cancellation-safe.

## Settings migration

New settings should use the typed DataStore repository. Legacy SharedPreferences values may be read for migration, but new code must not introduce additional scattered preference keys. Migration must preserve existing user configuration and provide deterministic defaults.

## Operator requirements

The operator must maintain explicit states, enforce capability/permission/risk policy before actions, expose an abort path, observe after meaningful actions, verify expected results, and stop rather than claim success when verification is inconclusive.

## Release gate

Do not declare NEXUS complete until build, tests, runtime availability, safety tests, API 28 compatibility, release packaging, and security audit have been verified.
