# AURA-X Operator

**Your Phone. Your Operator. Under Your Command.**

AURA-X Operator is a **sideloaded, local-first Android AI operator** built around **AccessibilityService**, **Jetpack Compose**, **Room**, **WorkManager**, and a **native llama.cpp bridge**. This project implements the **NEXUS v5.0** architecture with a **closed-loop Observe → Plan → Act → Verify → Correct** pipeline.

---

## **📌 Current Status**
- **Version**: 5.0.0 NEXUS (in development)
- **Minimum SDK**: Android 9 / API 28
- **Target/Compile SDK**: Android 14 / API 34 (compatible with API 35)
- **ABI**: arm64-v8a
- **Java**: 17
- **Native Toolchain**: CMake 3.31.6 / NDK 25.2.9519653
- **Distribution**: Direct APK / sideload only

---

## **🚀 NEXUS v5.0 Features**

### **Core Architecture**
- **Clean Architecture** + **MVVM** + **MVI** for complex stateful screens.
- **Hilt** for dependency injection.
- **Room** for persistence (conversations, messages, memories, tasks, safety events).
- **WorkManager** for background tasks (e.g., model downloads).
- **Kotlin Coroutines** + **Flow/StateFlow** for asynchronous programming.
- **Jetpack Compose** + **Material 3** + **AURA-X Glass Nexus** design system.

---

### **AI Capabilities**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Local LLM**             | ✅ Implemented   | llama.cpp JNI bridge for Qwen2.5-0.5B-Instruct (GGUF Q4_K_M).                                  |
| **Vision Runtime**        | ✅ Gated         | llava.cpp integration (placeholder JNI bindings in `llava-native.cpp`).                     |
| **STT (Whisper)**         | ✅ Implemented   | Whisper JNI bridge + Android SpeechRecognizer fallback.                                      |
| **TTS (Android TTS)**     | ✅ Implemented   | Android TTS with optional Piper integration.                                                 |
| **Wake Word Detection**   | ✅ Gated         | Porcupine integration (placeholder in `WakeWordManager.kt`).                                |
| **Barge-In**              | ✅ Implemented   | `AudioRecord`-based speech detection in `AndroidTTSEngine.kt`.                              |
| **Continuous Conversation** | ✅ Implemented | State machine in `ConversationManager.kt`.                                                   |

---

### **Operator Capabilities**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **AccessibilityService**  | ✅ Implemented   | UI inspection, click, scroll, type, navigation.                                               |
| **Safety Blocks**         | ✅ Implemented   | Password/OTP/payment fields blocked.                                                         |
| **Abort System**          | ✅ Implemented   | Volume Down, notification, overlay, Quick Settings.                                           |
| **Verification**          | ✅ Implemented   | Post-action verification.                                                                     |
| **Recovery**              | ✅ Implemented   | Replanning on failure.                                                                        |
| **Task Execution**        | ✅ Implemented   | Planner, executor, and state machine.                                                         |

---

### **Memory & RAG**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Memory System**         | ✅ Implemented   | Room-backed working, episodic, semantic memory.                                               |
| **Vector Search**         | ✅ Implemented   | Deterministic embeddings and top-K retrieval.                                                 |
| **Knowledge Base**        | ✅ Implemented   | PDF, TXT, Markdown, images, OCR ingestion.                                                     |
| **RAG Pipeline**          | ✅ Implemented   | Embed, store, index, and retrieve.                                                           |

---

### **Model Hub**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Hugging Face Integration** | ✅ Implemented | Search, browse, download, verify models.                                                     |
| **Default Model**         | ✅ Implemented   | Qwen2.5-0.5B-Instruct (GGUF Q4_K_M).                                                           |
| **Automatic Download**    | ✅ Implemented   | WorkManager-based, Wi-Fi-only, resumable downloads.                                           |
| **Model Lifecycle**       | ✅ Implemented   | Load, unload, cancel, delete, verify.                                                         |

---

### **Voice & Conversation**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **STT (Whisper)**         | ✅ Implemented   | Whisper JNI bridge + Android fallback.                                                        |
| **TTS (Android TTS)**     | ✅ Implemented   | Android TTS with optional Piper.                                                               |
| **Wake Word Detection**   | ✅ Gated         | Porcupine integration (placeholder).                                                          |
| **Barge-In**              | ✅ Implemented   | `AudioRecord`-based speech detection.                                                          |
| **Continuous Conversation** | ✅ Implemented | State machine with wake word and barge-in.                                                   |

---

### **LAN Server Mode**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **mDNS Discovery**        | ✅ Implemented   | `_aura-x._tcp` service discovery.                                                             |
| **QR Pairing**            | ✅ Implemented   | ZXing-based QR code generation/scanning.                                                     |
| **Model Inference**       | ✅ Implemented   | Streaming generation over LAN.                                                               |

---

### **Settings & UI**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **DataStore Settings**    | ✅ Implemented   | Granular controls for all features.                                                           |
| **Biometric Lock**        | ✅ Implemented   | Secure operator unlock.                                                                       |
| **Theme Support**         | ✅ Implemented   | Dark/light/system themes, AMOLED, reduced motion.                                             |
| **Widgets**               | ✅ Implemented   | Quick Action, Status, Task widgets.                                                            |
| **Quick Settings Tile**   | ✅ Implemented   | Enable/disable operator.                                                                      |
| **Onboarding**            | ✅ Implemented   | 10-step onboarding.                                                                            |

---

### **Diagnostics & Backup**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Application Logging**   | ✅ Implemented   | Persistent logs with rotation.                                                                |
| **Safety Log Export**     | ✅ Implemented   | CSV, JSON, TXT export.                                                                         |
| **Diagnostic Bundle**     | ✅ Implemented   | ZIP export with app metadata.                                                                 |
| **Encrypted Backup**      | ✅ Implemented   | Local backup with selective restore.                                                          |
| **Analytics Dashboard**   | ✅ Implemented   | Room-backed usage analytics.                                                                  |

---

### **Security**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Permission Audit**      | ✅ Implemented   | Contextual permission requests.                                                               |
| **Accessibility Safety**  | ✅ Implemented   | Password/OTP/payment fields blocked.                                                          |
| **Network Security**      | ✅ Implemented   | HTTPS, certificate validation.                                                                |
| **Data Security**         | ✅ Implemented   | App-private storage, encrypted backups.                                                       |

---

### **Testing & CI**
| **Feature**               | **Status**       | **Details**                                                                                     |
|---------------------------|------------------|-----------------------------------------------------------------------------------------------|
| **Unit Tests**            | ✅ Added         | Mock-based tests for VisionManager, WakeWordManager, LANServer.                              |
| **Lint**                  | ✅ Implemented   | Static analysis in CI workflow.                                                                |
| **Detekt**                | ✅ Implemented   | Kotlin static analysis.                                                                       |
| **CI Workflow**           | ✅ Implemented   | GitHub Actions for build, test, lint, detekt.                                                  |

---

## **📥 Model Hub**

The **Model Center** provides a **local-first Hugging Face workflow** for public repositories:

- Search public Hugging Face models.
- Browse repository files recursively.
- Filter repository files by **GGUF**, **SafeTensors**, or all files.
- Show model downloads, likes, tags, and pipeline metadata.
- Show file size and available **SHA-256/LFS hash**.
- Queue downloads through **WorkManager**.
- **Wi-Fi-only** download mode.
- **Resumable** `.part` downloads when the server supports HTTP Range.
- **HTTP 416 recovery** by restarting a stale partial download.
- Safe filesystem model names and stable Hub registry IDs.
- Expected-size validation when metadata is available.
- **SHA-256 verification** when the Hub exposes a hash.
- **GGUF magic/signature** and minimum-size validation before `READY`.
- Load, unload, cancel, and delete model lifecycle controls.
- Persistent Room model metadata and local paths.
- Refusal to load a model whose local file is missing.

---

### **Primary Model**
**Qwen 2.5 0.5B Instruct — GGUF Q4_K_M**

`Qwen/Qwen2.5-0.5B-Instruct-GGUF/qwen2.5-0.5b-instruct-q4_k_m.gguf`

The APK does **not bundle model weights**. Open **Settings → Model Center** and download/import a compatible model.

---

## **⚙️ Settings**

Settings are persisted locally through the **protected preferences layer** and expose an **explicit staged workflow** (Save/Discard):

- **Automation Policy**: Observe Only, Suggest Only, Confirm Actions, Full Auto Low Risk.
- **Confirmation Window**: Configurable countdown for medium/high-risk actions.
- **Task Limits**: Maximum actions per task, task duration.
- **Biometric Lock**: Secure operator unlock.
- **Operator Indicator**: Visual status indicator.
- **Incognito Protection**: Respect private browsing.
- **Password Filtering**: Block password-like fields.
- **Screenshot Verification**: Verify UI before actions.
- **Screen-Change Abort**: Stop if screen changes unexpectedly.
- **Model Settings**: Context length, temperature, top-p, top-k, output tokens, CPU threads, GPU acceleration, batch size.
- **Wake Word**: Enablement, sensitivity, continuous-conversation timeout.
- **STT/TTS**: Model and language controls.
- **Memory**: Retention, vector search, RAG, top-K retrieval.
- **Network**: Local-only mode, search/network controls.
- **Theme**: Dynamic colors, animations, reduced motion, font scale, orb style.
- **Performance**: Battery threshold, thermal protection, automatic model unload.
- **LAN Server**: Port, authentication, remote model URL.
- **Developer**: Debug/verbose logging, diagnostics.

---

## **🛡️ Safety Model**

AURA-X is **deliberately transparent**:
- Automation is **visible** (operator indicator, overlays).
- Automation is **abortable** (Volume Down, notification, Quick Settings).
- Automation is **audited** (safety logs, verification).

**Blocked Actions**:
- Password-like fields.
- Authentication/payment/security flows.
- Private browsing.

**Safety Policies**:
- **Observe Only**: No actions, only UI inspection.
- **Suggest Only**: Suggest actions but require manual confirmation.
- **Confirm Actions**: Confirm medium/high-risk actions.
- **Full Auto Low Risk**: Automate low-risk actions (e.g., scrolling, reading text).

---

## **🔌 Known Gated Capabilities**

Some capabilities are **intentionally gated** until their runtimes/models are integrated:

| **Capability**               | **Status**       | **Dependency**                     | **Notes**                                                                                     |
|------------------------------|------------------|-----------------------------------|-----------------------------------------------------------------------------------------------|
| **Vision Runtime**           | ✅ Gated         | llava.cpp                         | JNI bindings in place; await llava.cpp stabilization.                                         |
| **Wake Word Detection**      | ✅ Gated         | Porcupine                        | Integration code in place; await dependency uncommenting.                                   |
| **Direct SafeTensors**       | ❌ Not Bundled   | ONNX Runtime / TensorFlow Lite   | Not bundled; requires separate runtime.                                                      |
| **On-Device Conversion**     | ❌ Not Bundled   | Custom tooling                   | Not bundled; requires separate tooling.                                                       |
| **Arbitrary Plugin Code**    | ❌ Disabled      | N/A                               | Arbitrary downloaded code is **disabled** for security.                                      |

These gates are **deliberate safety and runtime-integrity boundaries**, not fake feature claims.

---

## **🏗️ Build**

Use **Android Studio Hedgehog or newer** with **JDK 17**.

```bash
# Build debug APK
./gradlew clean assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lintDebug

# Run detekt (static analysis)
./gradlew detektAll
```

The **CI workflow** is the source of truth for build verification. A source review is **not** treated as proof of a successful native Android build.

---

## **📚 Documentation**

- [`MODEL_SETUP.md`](MODEL_SETUP.md) — Local model setup and troubleshooting.
- [`docs/MODEL_HUB.md`](docs/MODEL_HUB.md) — Hugging Face and Model Hub workflow.
- [`docs/SETTINGS.md`](docs/SETTINGS.md) — Settings and persistence.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — System architecture.
- [`docs/A_TO_Z_AUDIT.md`](docs/A_TO_Z_AUDIT.md) — Project audit checklist.
- [`docs/RELEASE_GATE.md`](docs/RELEASE_GATE.md) — Pre-release verification gate.
- [`docs/NEXUS_UPGRADE_2026-08-14.md`](docs/NEXUS_UPGRADE_2026-08-14.md) — NEXUS upgrade notes.
- [`NEXUS_V5_IMPLEMENTATION.md`](NEXUS_V5_IMPLEMENTATION.md) — NEXUS v5.0 implementation progress.

---

## **📦 APK**

GitHub Actions → Latest **Android CI** run → **Artifacts** → `app-debug.apk` (when available).

Install the APK on an **Android 9+ arm64 device**, then:
1. Enable **AccessibilityService** (Settings → Accessibility → AURA-X Operator).
2. Enable **Overlay Access** (Settings → Apps → AURA-X Operator → Display over other apps).
3. Open the app and complete **onboarding**.

---

## **🚀 NEXUS v5.0 Development**

The **NEXUS v5.0** implementation is ongoing in the [`nexus-v5-integrations`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/tree/nexus-v5-integrations) branch. Key milestones:

1. **Integrate llava.cpp**: Uncomment `FetchContent` in `CMakeLists.txt` and replace placeholders in `llava-native.cpp`.
2. **Integrate Porcupine**: Uncomment dependency in `app/build.gradle.kts` and initialize in `WakeWordManager.kt`.
3. **Test Barge-In**: Verify `AudioRecord`-based speech detection in `AndroidTTSEngine.kt`.
4. **Test LAN Server**: Verify mDNS and QR pairing on real devices.
5. **Run CI Workflow**: Fix any failures in build, tests, lint, or detekt.

---

## **🤝 Contributing**

1. **Clone the repository**:
   ```bash
   git clone https://github.com/abdulraheemnohri/AURA-X-OPERATOR.git
   ```
2. **Check out the latest branch**:
   ```bash
   git checkout nexus-v5-integrations
   ```
3. **Build and test**:
   ```bash
   ./gradlew clean assembleDebug testDebugUnitTest
   ```
4. **Fix issues** and submit a pull request.

---

## **📜 License**

[LICENSE](LICENSE)

---

## **🙏 Acknowledgments**

- **llama.cpp**: [ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp)
- **whisper.cpp**: [ggml-org/whisper.cpp](https://github.com/ggml-org/whisper.cpp)
- **llava.cpp**: [ggml-org/llava.cpp](https://github.com/ggml-org/llava.cpp) (upcoming)
- **Porcupine**: [Picovoice/porcupine-android](https://github.com/Picovoice/porcupine-android)
- **ZXing**: [zxing/zxing](https://github.com/zxing/zxing)
- **Jetpack Compose**: [Android Compose](https://developer.android.com/jetpack/compose)
- **Hilt**: [Google Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Room**: [Android Room](https://developer.android.com/training/data-storage/room)
- **WorkManager**: [Android WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

---

## **📧 Contact**

- **Repository**: [abdulraheemnohri/AURA-X-OPERATOR](https://github.com/abdulraheemnohri/AURA-X-OPERATOR)
- **Issues**: [GitHub Issues](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/issues)
- **Discussions**: [GitHub Discussions](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/discussions)
