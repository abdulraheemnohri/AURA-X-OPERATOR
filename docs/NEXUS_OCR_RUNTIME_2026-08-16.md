# AURA-X NEXUS — OCR Runtime

## Status

Implemented a real on-device OCR runtime using the bundled ML Kit Latin text recognizer.

## Pipeline

```text
Screenshot Bitmap
      ↓
InputImage
      ↓
ML Kit Text Recognition
      ↓
OCR blocks + bounding boxes
      ↓
ScreenPerception
      ↓
Planner / verifier / recovery
```

## Runtime truth

`OcrRuntime.isAvailable()` is backed by the actual runtime instance. The application no longer reports OCR as unavailable merely because no native vision model exists.

Vision remains independently model/runtime gated.

## Supported OCR script

This implementation uses the bundled ML Kit Latin recognizer. It is not a claim of Urdu, Arabic, Hindi, Bengali, or other-script OCR support. Additional scripts require their corresponding compatible recognizer/runtime.

## Safety

OCR output is treated as perception evidence only. It does not grant permission to act. Sensitive authentication text is detected by the screen-perception layer and execution remains subject to capability, permission, risk, and policy gates.

## Failure behavior

OCR errors are returned as structured `OcrResult.error` values. Accessibility evidence remains available, so OCR failure does not make the operator blindly act on an empty screen.
