# AURA-X NEXUS — Semantic Screen Context

## Status

Implemented on `main`.

## Purpose

The operator previously passed mostly raw accessibility text into recovery planning. The NEXUS planner now receives a compact structured representation derived from the accessibility tree.

## Pipeline

```text
AccessibilityNodeInfo
        ↓
ScreenContextExtractor
        ↓
ScreenContext
        ↓
SemanticScreenContextBuilder
        ↓
SemanticScreenContext
        ↓
bounded planner context
        ↓
LocalModelPlanner.replan()
```

## Represented state

- package name
- window title
- visible text
- actionable UI elements
- element role
- element label
- resource ID when available
- editable/clickable state
- password/sensitive/private flags

## Safety

The semantic representation is derived from observed accessibility state. It does not invent visual facts and does not replace OCR or a vision runtime.

Sensitive/private screens are represented as blocked planner context and recovery is stopped before model replanning.

## Scope

This is structured accessibility understanding, not full visual understanding. Screenshot/OCR/vision fusion remains a separate capability-gated milestone.

## Next milestone

Add optional OCR/vision evidence to the same semantic screen contract while preserving accessibility as the primary low-cost observation source.
