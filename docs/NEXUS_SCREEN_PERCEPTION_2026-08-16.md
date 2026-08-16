# AURA-X NEXUS — Screen Perception

## Status

Implemented: an explicit screen-perception boundary with Accessibility as the current authoritative source.

OCR and vision are intentionally reported unavailable until real runtimes/models are integrated.

## Pipeline

```text
Accessibility tree
      ↓
ScreenContext
      ↓
ScreenPerception
      ↓
PerceivedScreen
      ├── text
      ├── actionable targets
      ├── package
      ├── confidence
      └── perception sources
      ↓
Planner / verifier / recovery
```

## Safety

Perceived text is checked for sensitive authentication material such as passwords, OTPs, PINs, CVV, secrets, API keys and tokens. The perception layer does not itself grant permission to act.

Execution must continue through the existing capability, permission, risk and policy gates.

## Runtime truth

`isOcrAvailable()` and `isVisionAvailable()` currently return `false`. This is deliberate. The application must not advertise OCR or vision as READY without a real runtime and compatible model.

## Next integration

1. OCR runtime abstraction and screenshot preprocessing.
2. Real OCR implementation.
3. Vision runtime abstraction.
4. Model-backed visual analysis.
5. Fusion of Accessibility + OCR + Vision evidence.
6. Confidence-aware planner and verifier decisions.
