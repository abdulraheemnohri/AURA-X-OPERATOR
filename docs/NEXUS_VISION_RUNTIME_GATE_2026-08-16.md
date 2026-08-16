# AURA-X NEXUS — Vision Runtime Gate

## Status

Vision remains `MODEL_GATED` until a real native multimodal runtime and compatible model are installed and validated.

## Current perception stack

- Accessibility provides actionable UI targets.
- OCR provides visual text evidence.
- Evidence fusion combines those sources.
- Vision is an additional evidence source, not an action authority.

## Required activation sequence

```text
Screenshot
  -> native vision runtime
  -> model load
  -> multimodal inference
  -> structured visual evidence
  -> evidence fusion
  -> planner / verifier
```

## Safety requirements

1. A missing native runtime must report unavailable.
2. A missing model must report model-gated.
3. Failed model loading must never produce a READY state.
4. Vision output cannot bypass capability, permission, risk, or policy gates.
5. Vision-only evidence cannot authorize sensitive or destructive actions.
6. Runtime failures must preserve the previous working perception path.
7. No placeholder visual answer may be returned as a successful inference.

## Acceptance criteria

The runtime may only transition to READY after:

- native library is present;
- model file exists;
- model format is validated;
- runtime initialization succeeds;
- a bounded smoke inference succeeds;
- failure state is persisted if initialization fails.

Until then the UI must expose Vision as MODEL_GATED / NOT_AVAILABLE rather than READY.
