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
6. **Updated Dependencies** (ZXing for QR codes)
7. **Updated CI Workflow** (Lint, Multi-branch support)

---

## **✅ Implemented Features**

### **1. Vision Runtime**
- **Status**: ✅ **Implemented** (gated by llava.cpp integration)
- **Files Added**:
  - [`VisionRuntime.kt`](app/src/main/java/com/aurax/operator/ai/vision/VisionRuntime.kt)
  - [`LlavaVisionRuntime.kt`](app/src/main/java/com/aurax/operator/ai/vision/LlavaVisionRuntime.kt)
  - [`VisionManager.kt`](app/src/main/java/com/aurax/operator/ai/vision/VisionManager.kt)
  - [`llava-native.cpp`](app/src/main/cpp/llava-native.cpp)
- **Features**:
  - Multimodal vision-language model support (image + text).
  - Fallback to OCR + Accessibility if vision model is unavailable.
  - JNI bridge for llava.cpp integration.
- **Dependencies**:
  - `llava.cpp` (to be integrated via CMake `FetchContent`).

---

### **2. Wake Word Detection**
- **Status**: ✅ **Implemented** (gated by Porcupine integration)
- **Files Added**:
  - [`WakeWordManager.kt`](app/src/main/java/com/aurax/operator/voice/wakeword/WakeWordManager.kt)
- **Features**:
  - Wake word detection using Porcupine (placeholder).
  - Configurable sensitivity and low-power mode.
  - Callback for wake word detection.
- **Dependencies**:
  - `ai.picovoice:porcupine-android:3.0.0` (commented out in `build.gradle.kts`).

---

### **3. Continuous Conversation**
- **Status**: ✅ **Implemented** (gated by wake word + barge-in)
- **Files Added**:
  - [`ConversationManager.kt`](app/src/main/java/com/aurax/operator/voice/conversation/ConversationManager.kt)
  - [`AndroidTTSEngine.kt`](app/src/main/java/com/aurax/operator/voice/tts/AndroidTTSEngine.kt)
- **Features**:
  - Continuous conversation state machine (IDLE → LISTENING → PROCESSING → SPEAKING).
  - Barge-in support (interrupt TTS to listen).
  - Wake word integration.

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
- **Files Added**:
  - [`VisionManagerTest.kt`](app/src/test/java/com/aurax/operator/ai/vision/VisionManagerTest.kt)
  - [`WakeWordManagerTest.kt`](app/src/test/java/com/aurax/operator/voice/wakeword/WakeWordManagerTest.kt)
  - [`LANServerTest.kt`](app/src/test/java/com/aurax/operator/network/lan/LANServerTest.kt)
- **Features**:
  - Mock-based tests for VisionManager, WakeWordManager, and LANServer.
  - Placeholder for actual runtime integrations.

---

### **6. Updated Dependencies**
- **File Modified**: [`app/build.gradle.kts`](app/build.gradle.kts)
- **Changes**:
  - Added `com.journeyapps:zxing-android-embedded:4.3.0` for QR code generation/scanning.
  - Commented out `ai.picovoice:porcupine-android:3.0.0` (to be uncommented when integrated).

---

### **7. Updated Manifest**
- **File Modified**: [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml)
- **Changes**:
  - Added `CAMERA` permission for QR code scanning.
  - Added `ACCESS_FINE_LOCATION` and `CHANGE_WIFI_MULTICAST_STATE` for LAN server discovery.

---

### **8. Updated CMakeLists.txt**
- **File Modified**: [`app/src/main/cpp/CMakeLists.txt`](app/src/main/cpp/CMakeLists.txt)
- **Changes**:
  - Added `FetchContent` for `llava.cpp` (to be integrated).
  - Added `aurax_llava` shared library for llava-native.cpp.

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
- **File Modified**: [`.github/workflows/android-ci.yml`](.github/workflows/android-ci.yml)
- **Changes**:
  - Added support for `nexus-v5` branch.
  - Added `lintDebug` step to run static analysis.
  - Updated verification to include `llava-native.cpp`.

---

## **🚀 Next Steps**

### **1. Integrate llava.cpp**
- **Task**: Replace the placeholder in `llava-native.cpp` with actual llava.cpp JNI calls.
- **Steps**:
  1. Uncomment and update `FetchContent` for `llava.cpp` in `CMakeLists.txt`.
  2. Implement `nativeLoad`, `nativeAnalyze`, and `nativeUnload` in `llava-native.cpp`.
  3. Test vision model loading and inference.

---

### **2. Integrate Porcupine**
- **Task**: Uncomment and integrate `ai.picovoice:porcupine-android:3.0.0` in `build.gradle.kts`.
- **Steps**:
  1. Uncomment the dependency in `app/build.gradle.kts`.
  2. Initialize Porcupine in `WakeWordManager.kt`.
  3. Test wake word detection.

---

### **3. Implement Barge-In**
- **Task**: Complete barge-in logic in `AndroidTTSEngine.kt`.
- **Steps**:
  1. Use `AudioRecord` to detect speech during TTS.
  2. Trigger `onBargeIn` callback when speech is detected.
  3. Test barge-in functionality.

---

### **4. Test LAN Server Mode**
- **Task**: Verify mDNS discovery and QR pairing.
- **Steps**:
  1. Test `LANServer.start()` and `LANServer.stop()`.
  2. Test `QRPairingManager.generatePairingQR()` and `parsePairingQR()`.
  3. Test model list and inference endpoints.

---

### **5. Run Unit Tests**
- **Task**: Execute `./gradlew testDebugUnitTest`.
- **Expected Outcome**:
  - All new unit tests pass.
  - Fix any failures or mock issues.

---

### **6. Build Debug APK**
- **Task**: Run `./gradlew assembleDebug`.
- **Expected Outcome**:
  - Debug APK builds successfully.
  - Fix any compilation or linking errors.

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
- [x] Continuous Conversation implemented (gated by barge-in).
- [x] LAN Server Mode implemented (mDNS + QR pairing).
- [x] Unit tests added for new components.
- [x] Dependencies updated (ZXing, Porcupine).
- [x] Manifest updated (CAMERA, LAN permissions).
- [x] CMakeLists.txt updated (llava.cpp).
- [x] DI modules updated (VisionRuntime, LANServer).
- [x] CI workflow updated (lint, multi-branch support).
- [ ] llava.cpp integrated and tested.
- [ ] Porcupine integrated and tested.
- [ ] Barge-in implemented and tested.
- [ ] LAN Server Mode tested.
- [ ] Unit tests pass.
- [ ] Debug APK builds.
- [ ] Security audit complete.

---

## **🔗 Useful Links**

- **Repository**: [abdulraheemnohri/AURA-X-OPERATOR](https://github.com/abdulraheemnohri/AURA-X-OPERATOR)
- **Branch**: [`nexus-v5`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/tree/nexus-v5)
- **Commits**: [Compare `main`...`nexus-v5`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/compare/main...nexus-v5)
- **llava.cpp**: [ggml-org/llava.cpp](https://github.com/ggml-org/llava.cpp)
- **Porcupine**: [Picovoice/porcupine-android](https://github.com/Picovoice/porcupine-android)
- **ZXing**: [zxing/zxing](https://github.com/zxing/zxing)

---

## **📝 Notes**

1. **llava.cpp Integration**: The `llava-native.cpp` file includes placeholder JNI functions. Replace these with actual calls to llava.cpp once integrated.
2. **Porcupine Integration**: The `WakeWordManager` uses a placeholder for Porcupine. Uncomment the dependency and initialize Porcupine to enable wake word detection.
3. **Barge-In**: The `AndroidTTSEngine` includes a placeholder for barge-in. Implement `AudioRecord`-based speech detection to enable barge-in.
4. **LAN Server Testing**: Test mDNS discovery and QR pairing on a local network with multiple devices.
5. **Unit Tests**: The new unit tests use Mockito for mocking. Ensure Mockito is properly configured in the test dependencies.
6. **CI Workflow**: The CI workflow now supports the `nexus-v5` branch and includes lint checks.

---

## **🎯 Next Milestones**

| **Milestone**               | **Status**       | **ETA**          |
|----------------------------|------------------|------------------|
| llava.cpp Integration      | ❌ Not Started   | 1-2 days         |
| Porcupine Integration       | ❌ Not Started   | 1 day           |
| Barge-In Implementation     | ❌ Not Started   | 1 day           |
| LAN Server Testing          | ❌ Not Started   | 1 day           |
| Unit Tests Pass            | ❌ Not Started   | 1 day           |
| Debug APK Build            | ❌ Not Started   | 1 day           |
| Security Audit              | ❌ Not Started   | 1 day           |
| **NEXUS v5.0 Release**     | ❌ Not Started   | 1 week          |
