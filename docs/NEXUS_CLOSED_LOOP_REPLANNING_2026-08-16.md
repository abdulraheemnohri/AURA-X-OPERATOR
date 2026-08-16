# AURA-X NEXUS — Closed-Loop Replanning

## Status

Implemented in `main` on 2026-08-16.

## Execution model

A failed verification no longer immediately terminates the task when a safe local-model recovery plan can be produced.

```text
ACT
 ↓
OBSERVE
 ↓
VERIFY
 ↓
 ├─ success → NEXT STEP
 │
 └─ failure
      ↓
   re-observe (bounded)
      ↓
   inspect current screen
      ↓
   safety/private-state gate
      ↓
   local model recovery planner
      ↓
   validate allow-listed replacement plan
      ↓
   replace failed step
      ↓
   ACT
      ↓
   OBSERVE
      ↓
   VERIFY
```

## Safety boundaries

Recovery is not an escape hatch around the normal safety pipeline.

Every replacement step still passes through:

- sensitive-data detection
- blocked-package detection
- risk classification
- automation policy
- confirmation policy
- runtime action limits
- post-action verification

The local recovery planner accepts only the existing allow-listed tools and arguments. It rejects sensitive/private content and invalid tool schemas.

## Replanning limits

A task may perform at most two plan replacements in one execution. This prevents infinite recovery loops.

If recovery cannot produce a safe replacement plan, the task fails safely and is persisted as failed.

## Observed context

The recovery prompt receives only bounded screen text plus:

- original user request
- failed step description
- verification failure evidence

Sensitive/private screen states stop recovery rather than being forwarded to the planner.

## Important limitation

This is bounded local-model replanning, not unrestricted autonomous planning. The current planner remains constrained to the repository's allow-listed operator tools. New tool classes must be added deliberately through the normal capability, permission, risk, and policy architecture.
