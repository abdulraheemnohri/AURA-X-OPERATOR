# AURA-X NEXUS — Operator Verification

## Purpose

The operator now treats tool success as execution evidence, not proof that the requested state was reached.

## Execution loop

```text
Plan
 ↓
Policy / Risk Gate
 ↓
Action
 ↓
OBSERVE
 ↓
VERIFY
 ├─ pass → next step
 └─ fail → bounded re-observation
              ├─ recovered → next step
              └─ failed → task fails safely
```

## Evidence

`ActionVerifier` uses the post-action accessibility state and accepts explicit evidence such as:

- configured `verifyText`
- configured/derived expected package
- active browser window after `open_url`

Sensitive/private screens are never accepted as successful verification.

## Safety behavior

An action that cannot produce a reliable verification signal is not allowed to silently become a successful task. The executor records verification events and fails safely after bounded re-observation.

## Current limitation

This tranche implements bounded observe/re-observe recovery. Full planner-level re-planning from a changed screen remains a separate phase and must not be represented as complete until the planner can consume the new `ScreenContext` and generate a replacement plan.
