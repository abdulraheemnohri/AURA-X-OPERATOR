# AURA-X NEXUS Upgrade — 2026-08-14

## Added

- Engine-neutral wake phrase gate and continuous conversation state machine.
- File-backed local knowledge base with chunking, deterministic local embeddings and top-K cosine retrieval.
- Chat integration that injects relevant local knowledge into task execution when retrieval returns matches.
- AES-GCM encrypted local backup export with staged restore storage.
- Room-backed analytics aggregation for tasks, completion/failure rate, memories and safety events.
- Loopback-only authenticated companion HTTP server bound to `127.0.0.1`.
- Android Quick Settings tile for emergency abort and cockpit access.
- Nexus Upgrade Center UI for the new controls.
- Persistent local companion auth token generation.
- Wake-word unit tests.
- Manifest cleanup for the Quick Settings service.

## Intentionally still gated

- Real microphone wake-word inference still requires an audio/TFLite or equivalent model/runtime.
- Multimodal vision still requires a compatible local vision model/runtime.
- Backup restore is staged; applying a decrypted archive to live Room state remains a separate release gate.
- Full plugin SDK/arbitrary third-party plugins remain intentionally guarded.
- Home-screen widgets are not included yet.
- Safetensors runtime and model conversion/quantization are not bundled.
- Native llama.cpp/device smoke validation requires a real Android build/device runner.

## Validation status

Repository source-level integration was reviewed through GitHub. No successful CI/build result is claimed for this direct-to-main update because the available workflow lookup did not report a run for the latest direct commits.
