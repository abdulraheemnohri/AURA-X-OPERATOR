# AURA-X NEXUS — Capability Dual-Gating Pass

Date: 2026-08-16

## Purpose

NEXUS capabilities that depend on both an asset and an executable runtime must not become available when only one side exists.

## Implemented

`NexusCapability` now supports independent requirements for:

- permissions
- model assets
- native/optional runtimes

The registry availability calculation requires every declared requirement to be satisfied.

## Covered capabilities

- Whisper: `whisper-model` + `whisper-runtime`
- Vision image understanding: `vision-model` + `vision-runtime`
- Embeddings: `embedding-model` + `embedding-runtime`
- llama.cpp: `llama.cpp + GGUF` + `llama.cpp`
- Wake word: `wake-word-engine`
- Memory graph: `memory-graph`

## Why this matters

A model file is not proof that an inference backend can execute it. Conversely, a native runtime is not proof that a compatible model is installed. The capability registry now treats those as separate gates.

This prevents the UI, planner, and settings surfaces from advertising a model-dependent feature as available when the executable runtime is missing.

## Remaining work

The repository still needs a real bundled Vision runtime before Vision can become available. The current llava JNI boundary deliberately reports the runtime as unavailable until a real implementation is linked.

Embedding runtime, wake-word engine, and memory-graph runtime also remain separately gated until their actual implementations are present.
