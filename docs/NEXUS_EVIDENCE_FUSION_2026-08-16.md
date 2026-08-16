# AURA-X NEXUS — Evidence Fusion

## Purpose

The operator now combines Accessibility and OCR as independent perception sources.

```text
Accessibility ─────┐
                   ├──> EvidenceFusionEngine ──> PerceivedScreen
OCR ───────────────┘
```

## Authority boundary

Accessibility remains authoritative for actionable targets because it exposes the Android UI elements that the operator can actually act on.

OCR contributes visual text evidence. OCR text alone must not authorize a destructive, credential-related, payment, OTP, password, or other sensitive action.

## Fusion behavior

The fusion layer:

- normalizes whitespace
- removes duplicate text evidence
- performs conservative textual similarity matching
- preserves evidence sources
- calculates accessibility/OCR agreement
- calculates a bounded perception confidence score
- propagates sensitive-content detection
- preserves the existing OCR error for diagnostics

## Confidence

- Accessibility only: high baseline confidence
- Accessibility + OCR: confidence increases with textual agreement
- OCR only: lower confidence
- no textual evidence: conservative fallback confidence

Confidence is advisory. It does not bypass the existing capability, permission, risk, confirmation, or verification gates.

## Next integration

The next perception milestone is a real Vision runtime. Vision remains model/runtime gated and must not be reported as READY until an actual compatible runtime is installed and healthy.
