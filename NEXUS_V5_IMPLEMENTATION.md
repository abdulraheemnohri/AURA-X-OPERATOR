# AURA-X NEXUS v5.0 Implementation

This document outlines the **NEXUS v5.0** implementation progress for **AURA-X Operator**, including new features, changes, and next steps.

---

## **📌 Overview**
**AURA-X NEXUS v5.0** is a **local-first, sideloaded Android AI operator** with a **closed-loop Observe → Plan → Act → Verify → Correct** architecture. This implementation builds on the existing **v4.0** codebase and adds the following key features:

1. **Vision Runtime** (llava.cpp integration)
2. **Wake Word Detection** (Porcupine integration)
3. **Continuous Conversation** (Barge-in support)
4. **LAN Server Mode** (mDNS + QR pairing)
5. **Enhanced Unit Tests** (Vision, Wake Word, LAN Server)
6. **Updated Dependencies** (ZXing for QR codes, Mockito for tests, detekt for static analysis)
7. **Updated CI Workflow** (Lint, Detekt, Multi-branch support)

---

## **✅ Implemented Features**

### **1. Vision Runtime**
- **Status**: ✅ **Implemented** (gated by llava.cpp integration)
- **Files Added/Modified**:
  - [`VisionRuntime.kt`](app/src/main/java/com/aurax/operator/ai/vision/VisionRuntime.kt)
  - [`LlavaVisionRuntime.kt`](app/src/main/java/com/aurax/operator/ai/vision/LlavaVisionRuntime.kt) (updated with `nativeGetStatus`)
  - [`VisionManager.kt`](app/src/main/java/com/aurax/operator/ai/vision/VisionManager.kt)
  - [`llava-native.cpp`](app/src/main/cpp/llava-native.cpp) (updated with improved JNI bindings)
- **Features**:
  - Multimodal vision-language model support (image + text).
  - Fallback to OCR + Accessibility if vision model is unavailable.
  - JNI bridge for llava.cpp integration.
  - Status tracking (`NOT_LOADED`, `LOADING`, `READY`, `ERROR`).
- **Dependencies**:
  - `llava.cpp` (to be integrated via CMake `FetchContent`).

---

### **2. Wake Word Detection**
- **Status**: ✅ **Implemented** (gated by Porcupine integration)
- **Files Added/Modified**:
  - [`WakeWordManager.kt`](app/src/main/java/com/aurax/operator/voice/wakeword/WakeWordManager.kt) (updated with full Porcupine integration placeholders)
- **Features**:
  - Wake word detection using Porcupine (placeholder).
  - Configurable sensitivity and low-power mode.
  - Callback for wake word detection.
  - Resource cleanup (`release()` method).
- **Dependencies**:
  - `ai.picovoice:porcupine-android:3.0.0` (commented out in `app/build.gradle.kts`).

---

### **3. Continuous Conversation**
- **Status**: ✅ **Implemented** (barge-in support added)
- **Files Added/Modified**:
  - [`ConversationManager.kt`](app/src/main/java/com/aurax/operator/voice/conversation/ConversationManager.kt) (updated with barge-in and wake word integration)
  - [`AndroidTTSEngine.kt`](app/src/main/java/com/aurax/operator/voice/tts/AndroidTTSEngine.kt) (updated with `AudioRecord`-based barge-in)
- **Features**:
  - Continuous conversation state machine (IDLE → LISTENING → PROCESSING → SPEAKING).
  - Barge-in support (interrupt TTS to listen using `AudioRecord`).
  - Wake word integration.
  - Resource cleanup (`release()` method).

---

### **4. LAN Server Mode**
- **Status**: ✅ **Implemented** (mDNS + QR pairing)
- **Files Added**:
  - [`LANServer.kt`](app/src/main/java/com/aurax/operator/network/lan/LANServer.kt)
  - [`QRPairingManager.kt`](app/src/main/java/com/aurax/operator/network/lan/QRPairingManager.kt)
  - [`NetworkModule.kt`](app/src/main/java/com/aurax/operator/di/NetworkModule.kt)
- **Features**:
  - mDNS service discovery (`_aura-x._tcp`).
  - QR code pairing for easy connection.
  - Model list and inference endpoints.
- **Dependencies**:
  - `com.journeyapps:zxing-android-embedded:4.3.0` (for QR codes).

---

### **5. Unit Tests**
- **Status**: ✅ **Added**
- **Files Added/Modified**:
  - [`VisionManagerTest.kt`](app/src/test/java/com/aurax/operator/ai/vision/VisionManagerTest.kt) (updated with Mockito Kotlin)
  - [`WakeWordManagerTest.kt`](app/src/test/java/com/aurax/operator/voice/wakeword/WakeWordManagerTest.kt) (updated with Mockito Kotlin)
  - [`LANServerTest.kt`](app/src/test/java/com/aurax/operator/network/lan/LANServerTest.kt) (updated with Mockito Kotlin)
  - [`MockitoHelpers.kt`](app/src/test/java/com/aurax/operator/testutils/MockitoHelpers.kt) (added for simplified mocking)
- **Features**:
  - Mock-based tests for VisionManager, WakeWordManager, and LANServer.
  - Mockito Kotlin helpers for simplified mocking.

---

### **6. Updated Dependencies**
- **File Modified**: [`app/build.gradle.kts`](app/build.gradle.kts)
- **Changes**:
  - Added `com.journeyapps:zxing-android-embedded:4.3.0` for QR code generation/scanning.
  - Added Mockito dependencies (`mockito-core`, `mockito-kotlin`, `mockito-inline`).
  - Added `detekt` plugin and `detekt-formatting` dependency.
  - Commented out `ai.picovoice:porcupine-android:3.0.0` (to be uncommented when integrated).

---

### **7. Updated Manifest**
- **File Modified**: [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml)
- **Changes**:
  - Added `CAMERA` permission for QR code scanning.
  - Added `ACCESS_FINE_LOCATION` and `CHANGE_WIFI_MULTICAST_STATE` for LAN server discovery.
  - `RECORD_AUDIO` already included for barge-in.

---

### **8. Updated CMakeLists.txt**
- **File Modified**: [`app/src/main/cpp/CMakeLists.txt`](app/src/main/cpp/CMakeLists.txt)
- **Changes**:
  - Added `aurax_llava` shared library for `llava-native.cpp`.
  - Commented out `FetchContent` for `llava.cpp` (to be uncommented once stable).
  - Properly linked `aurax_llava` with `log` and `android`.

---

### **9. Updated DI Modules**
- **Files Modified**:
  - [`RuntimeModule.kt`](app/src/main/java/com/aurax/operator/di/RuntimeModule.kt)
  - [`NetworkModule.kt`](app/src/main/java/com/aurax/operator/di/NetworkModule.kt)
- **Changes**:
  - Added `VisionRuntime` provider.
  - Added `LANServer` and `QRPairingManager` providers.

---

### **10. Updated CI Workflow**
- **Files Modified/Added**:
  - [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml) (updated with lint and multi-branch support)
  - [`.github/workflows/detekt.yml`](.github/workflows/detekt.yml) (added for static analysis)
- **Changes**:
  - Added support for `nexus-v5` and `nexus-v5-integrations` branches.
  - Added `lintDebug` step to run static analysis.
  - Updated verification to include `llava-native.cpp`.
  - Added detekt workflow for Kotlin static analysis.

---

### **11. Added Detekt Configuration**
- **Files Added**:
  - [`detekt.yml`](detekt.yml) (custom rules for code quality)
- **Features**:
  - Static code analysis for Kotlin.
  - Custom rules for complexity, style, naming, and potential bugs.

---

## **🚀 Next Steps**

### **1. Test the Build Locally**
Run the following commands to verify the build:
```bash
git clone -b nexus-v5-integrations https://github.com/abdulraheemnohri/AURA-X-OPERATOR.git
cd AURA-X-OPERATOR
./gradlew clean assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew detektAll
```

---

### **2. Integrate llava.cpp**
- **Task**: Replace the placeholder in `llava-native.cpp` with actual llava.cpp JNI calls.
- **Steps**:
  1. Uncomment `FetchContent` for `llava.cpp` in `CMakeLists.txt`.
  2. Replace placeholders in `llava-native.cpp` with actual llava.cpp API calls.
  3. Test vision model loading and inference.

---

### **3. Integrate Porcupine**
- **Task**: Uncomment and integrate `ai.picovoice:porcupine-android:3.0.0` in `app/build.gradle.kts`.
- **Steps**:
  1. Uncomment the dependency in `app/build.gradle.kts`.
  2. Uncomment Porcupine initialization in `WakeWordManager.kt`.
  3. Test wake word detection.

---

### **4. Test Barge-In**
- **Task**: Verify barge-in functionality.
- **Steps**:
  1. Test `AndroidTTSEngine.speak()` with barge-in enabled.
  2. Verify that speech during TTS triggers `onBargeIn`.
  3. Test `ConversationManager.handleBargeIn()`.

---

### **5. Test LAN Server Mode**
- **Task**: Verify mDNS discovery and QR pairing.
- **Steps**:
  1. Test `LANServer.start()` and `LANServer.stop()`.
  2. Test `QRPairingManager.generatePairingQR()` and `parsePairingQR()`.
  3. Test model list and inference endpoints on a local network.

---

### **6. Run CI Workflow**
- **Task**: Verify that the CI workflow passes for the `nexus-v5-integrations` branch.
- **Steps**:
  1. Push changes to `nexus-v5-integrations`.
  2. Check GitHub Actions for the **Android CI** and **Detekt** workflows.
  3. Fix any failures in build, tests, lint, or detekt.

---

### **7. Final Security Audit**
- **Task**: Perform a security audit before release.
- **Checklist**:
  - Permissions (no unnecessary permissions).
  - AccessibilityService (no sensitive data logging).
  - Network calls (HTTPS, certificate validation).
  - File access (no arbitrary paths).
  - Model downloads (checksum validation).

---

## **📊 Definition of Done (DoD)**

The **NEXUS v5.0** implementation is **complete** when:

- [x] Vision Runtime implemented (gated by llava.cpp).
- [x] Wake Word Detection implemented (gated by Porcupine).
- [x] Continuous Conversation implemented (barge-in support added).
- [x] LAN Server Mode implemented (mDNS + QR pairing).
- [x] Unit tests added for new components.
- [x] Dependencies updated (ZXing, Mockito, detekt).
- [x] Manifest updated (CAMERA, LAN permissions, RECORD_AUDIO).
- [x] CMakeLists.txt updated (aurax_llava library).
- [x] DI modules updated (VisionRuntime, LANServer).
- [x] CI workflow updated (lint, detekt, multi-branch support).
- [x] Detekt configuration added.
- [x] Barge-in implementation added.
- [x] Wake Word Manager updated with Porcupine placeholders.
- [x] Conversation Manager updated with barge-in and wake word integration.
- [ ] llava.cpp integrated and tested.
- [ ] Porcupine integrated and tested.
- [ ] Barge-in tested.
- [ ] LAN Server Mode tested.
- [ ] Unit tests pass.
- [ ] Debug APK builds.
- [ ] Detekt passes.
- [ ] Security audit complete.

---

## **🔗 Useful Links**

- **Repository**: [abdulraheemnohri/AURA-X-OPERATOR](https://github.com/abdulraheemnohri/AURA-X-OPERATOR)
- **Branches**:
  - [`nexus-v5`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/tree/nexus-v5) (stable)
  - [`nexus-v5-integrations`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/tree/nexus-v5-integrations) (latest)
- **Pull Requests**:
  - [PR #94](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/pull/94) (Merges `nexus-v5` into `main`)
- **Commits**: [Compare `main`...`nexus-v5-integrations`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/compare/main...nexus-v5-integrations)
- **External Dependencies**:
  - [llava.cpp](https://github.com/ggml-org/llava.cpp)
  - [Porcupine](https://github.com/Picovoice/porcupine-android)
  - [ZXing](https://github.com/zxing/zxing)
  - [Mockito](https://site.mockito.org/)
  - [Detekt](https://detekt.dev/)

---

## **📝 Notes**

1. **llava.cpp Integration**: The `llava-native.cpp` file includes improved JNI bindings with placeholders for actual llava.cpp API calls. Once llava.cpp stabilizes, uncomment `FetchContent` in `CMakeLists.txt` and replace the placeholders.

2. **Porcupine Integration**: The `WakeWordManager` includes full Porcupine integration code (commented out). Uncomment the dependency in `app/build.gradle.kts` and the initialization code in `WakeWordManager.kt` to enable wake word detection.

3. **Barge-In**: The `AndroidTTSEngine` now includes `AudioRecord`-based speech detection for barge-in. Test this functionality to ensure it works reliably.

4. **LAN Server Testing**: Test mDNS discovery and QR pairing on a local network with multiple devices. The `LANServer` and `QRPairingManager` are implemented but untested.

5. **Unit Tests**: The new unit tests use Mockito Kotlin for mocking. Ensure Mockito is properly configured in the test dependencies.

6. **CI Workflow**: The CI workflow now includes lint and detekt checks. Fix any issues reported by these tools.

7. **Detekt Configuration**: The `detekt.yml` file includes custom rules for code quality. Review and adjust these rules as needed.

---

## **🎯 Next Milestones**

| **Milestone**               | **Status**       | **ETA**          | **Branch**                     |
|----------------------------|------------------|------------------|--------------------------------|
| Test Build Locally         | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Integrate llava.cpp        | ❌ Not Started   | 1-2 days         | `nexus-v5-integrations`        |
| Integrate Porcupine        | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Test Barge-In              | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Test LAN Server Mode       | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Run CI Workflow            | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Unit Tests Pass            | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Debug APK Builds           | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Detekt Passes              | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Security Audit             | ❌ Not Started   | 1 day           | `nexus-v5-integrations`        |
| Merge PR #94               | ❌ Not Started   | 1 day           | `main`                          |
| **NEXUS v5.0 Release**     | ❌ Not Started   | 1 week          | `main`                          |

---

## **🔥 Summary of Changes in `nexus-v5-integrations`**

### **New Features**
1. **Barge-In Support**: Added `AudioRecord`-based speech detection in `AndroidTTSEngine.kt`.
2. **Improved JNI Bindings**: Updated `llava-native.cpp` with better error handling and status tracking.
3. **Full Porcupine Placeholders**: Updated `WakeWordManager.kt` with complete Porcupine integration code (commented out).
4. **Detekt Configuration**: Added `detekt.yml` and `.github/workflows/detekt.yml` for static analysis.

### **Updated Files**
- `app/src/main/java/com/aurax/operator/ai/vision/LlavaVisionRuntime.kt`
- `app/src/main/cpp/llava-native.cpp`
- `app/src/main/java/com/aurax/operator/voice/wakeword/WakeWordManager.kt`
- `app/src/main/java/com/aurax/operator/voice/tts/AndroidTTSEngine.kt`
- `app/src/main/java/com/aurax/operator/voice/conversation/ConversationManager.kt`
- `app/build.gradle.kts`
- `app/src/main/cpp/CMakeLists.txt`

### **New Files**
- `app/src/test/java/com/aurax/operator/testutils/MockitoHelpers.kt`
- `.github/workflows/detekt.yml`
- `detekt.yml`

---

## **📌 How to Contribute**
1. **Clone the `nexus-v5-integrations` branch**:
   ```bash
   git clone -b nexus-v5-integrations https://github.com/abdulraheemnohri/AURA-X-OPERATOR.git
   ```
2. **Test the build**:
   ```bash
   ./gradlew clean assembleDebug
   ```
3. **Run unit tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. **Fix any issues** and push changes to `nexus-v5-integrations`.
5. **Integrate llava.cpp and Porcupine** once ready.

---

## **🎉 Final Goal**
The **AURA-X NEXUS v5.0** implementation will be **complete** when:
- All features are implemented and tested.
- The CI workflow passes for all branches.
- The debug APK builds and runs successfully on Android 9+ devices.
- The security audit is complete.

**Result**: A **real, buildable, testable, sideloadable Android application** that serves as a **local-first AI operator** with **closed-loop Observe → Plan → Act → Verify → Correct** architecture.
