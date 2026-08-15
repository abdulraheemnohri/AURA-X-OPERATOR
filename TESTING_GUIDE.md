# AURA-X NEXUS v5.0 Testing Guide

This guide outlines how to **test the new features** implemented in **AURA-X NEXUS v5.0**. It covers:

1. **Local Build Testing**
2. **Vision Runtime Testing**
3. **Wake Word Detection Testing**
4. **Barge-In Testing**
5. **LAN Server Mode Testing**
6. **Unit Tests**
7. **CI Workflow Testing**

---

## **📌 Prerequisites**
Before testing, ensure you have:
- **Android Studio Hedgehog or newer** with **JDK 17**.
- **Android 9+ (API 28+) arm64 device** or emulator.
- **Git** and **Gradle 8.2+** installed.
- **Cloned the `nexus-v5-integrations` branch**:
  ```bash
  git clone -b nexus-v5-integrations https://github.com/abdulraheemnohri/AURA-X-OPERATOR.git
  cd AURA-X-OPERATOR
  ```

---

## **🚀 1. Local Build Testing**
### **1.1 Build Debug APK**
Run the following command to build the debug APK:
```bash
./gradlew clean assembleDebug
```
**Expected Outcome**:
- ✅ Build succeeds without errors.
- ✅ `app-debug.apk` is generated in `app/build/outputs/apk/debug/`.

**Troubleshooting**:
- If **build fails**, check for compilation errors in the logs.
- If **native build fails**, ensure NDK 25.2.9519653 and CMake 3.31.6 are installed.
- If **linker errors occur**, verify that all JNI functions are properly declared in `llava-native.cpp`.

---

### **1.2 Install APK on Device**
Install the APK on your Android 9+ device:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```
**Expected Outcome**:
- ✅ APK installs successfully.
- ✅ App launches without crashes.

**Troubleshooting**:
- If **installation fails**, ensure `adb` is working and USB debugging is enabled.
- If **app crashes on launch**, check `adb logcat` for errors.

---

### **1.3 Enable Required Permissions**
After installing the APK:
1. **Enable AccessibilityService**:
   - Go to **Settings → Accessibility → AURA-X Operator** and enable it.
2. **Enable Overlay Access**:
   - Go to **Settings → Apps → AURA-X Operator → Display over other apps** and enable it.
3. **Grant Camera Permission**:
   - Open the app and grant **CAMERA** permission when prompted (for QR code scanning).
4. **Grant Microphone Permission**:
   - Open the app and grant **RECORD_AUDIO** permission when prompted (for barge-in).

---

## **👁️ 2. Vision Runtime Testing**
### **2.1 Test Vision Runtime Initialization**
1. Open the app and navigate to **Settings → AI Engine → Vision Runtime**.
2. Verify that the **Vision Runtime status** is `NOT_LOADED` (since llava.cpp is not yet integrated).

**Expected Outcome**:
- ✅ Vision Runtime status is `NOT_LOADED`.
- ✅ No crashes when accessing Vision Runtime settings.

---

### **2.2 Test Vision Runtime Fallback**
1. Open the app and navigate to **Chat** or **Operator** screen.
2. Take a screenshot (or use the app's screenshot feature).
3. Verify that the app **falls back to OCR + Accessibility** for screen understanding.

**Expected Outcome**:
- ✅ App uses OCR and Accessibility tree for screen understanding.
- ✅ No errors or crashes when analyzing screens.

---

### **2.3 Test Vision Runtime with llava.cpp (Future)**
Once **llava.cpp is integrated**:
1. Place a **compatible llava model** (e.g., `llava-v1.5-7b.gguf`) in the app's model directory.
2. Load the model in **Settings → AI Engine → Vision Runtime**.
3. Verify that the **Vision Runtime status** changes to `READY`.
4. Take a screenshot and verify that the app uses **vision model** for multimodal understanding.

**Expected Outcome**:
- ✅ Vision Runtime status changes to `READY`.
- ✅ Vision model analyzes screens and returns descriptions/labels.

---

## **🎤 3. Wake Word Detection Testing**
### **3.1 Test Wake Word Detection (Placeholder)**
1. Open the app and navigate to **Settings → Voice & Speech → Wake Word**.
2. Enable **Wake Word Detection** and set sensitivity to **0.5**.
3. Verify that the **Wake Word status** is `NOT_AVAILABLE` (since Porcupine is not yet integrated).

**Expected Outcome**:
- ✅ Wake Word status is `NOT_AVAILABLE`.
- ✅ No crashes when enabling/disabling wake word detection.

---

### **3.2 Test Wake Word Detection with Porcupine (Future)**
Once **Porcupine is integrated**:
1. Ensure the **Porcupine dependency** is uncommented in `app/build.gradle.kts`.
2. Open the app and navigate to **Settings → Voice & Speech → Wake Word**.
3. Enable **Wake Word Detection** and select a wake word (e.g., "Hey AURA").
4. Say the wake word and verify that the app **starts listening** for commands.

**Expected Outcome**:
- ✅ Wake Word status changes to `AVAILABLE`.
- ✅ App detects wake word and starts listening.

---

## **🔊 4. Barge-In Testing**
### **4.1 Test Barge-In During TTS**
1. Open the app and navigate to **Settings → Voice & Speech → Continuous Conversation**.
2. Enable **Continuous Conversation** and **Barge-In**.
3. Start a conversation (e.g., say "Hello" or type a message).
4. While the app is **speaking a response**, speak again (e.g., say "Stop").
5. Verify that the app **stops speaking** and starts listening for a new command.

**Expected Outcome**:
- ✅ App detects speech during TTS and triggers barge-in.
- ✅ TTS stops, and the app starts listening for a new command.

**Troubleshooting**:
- If **barge-in does not work**, check `adb logcat` for errors in `AndroidTTSEngine`.
- Ensure **RECORD_AUDIO** permission is granted.
- Verify that the **amplitude threshold** (5000) is appropriate for your microphone.

---

### **4.2 Test Barge-In with Headphones**
1. Connect **headphones with a microphone** to your device.
2. Repeat the **Barge-In test** (Section 4.1).
3. Verify that barge-in works with headphones.

**Expected Outcome**:
- ✅ Barge-in works with headphones.

---

## **🌐 5. LAN Server Mode Testing**
### **5.1 Test LAN Server Start/Stop**
1. Open the app and navigate to **Settings → Network → LAN Server**.
2. Enable **LAN Server** and set a port (e.g., `8080`).
3. Tap **Start Server** and verify that the server starts.
4. Tap **Stop Server** and verify that the server stops.

**Expected Outcome**:
- ✅ LAN Server starts and stops without errors.
- ✅ Server status changes between `RUNNING` and `STOPPED`.

**Troubleshooting**:
- If **server fails to start**, check `adb logcat` for errors in `LANServer`.
- Ensure **Wi-Fi is connected** (LAN Server requires a network).
- Verify that the **port is not in use** by another app.

---

### **5.2 Test mDNS Discovery**
1. Start the LAN Server on **Device A** (as described in Section 5.1).
2. On **Device B** (connected to the same Wi-Fi network), open a **network scanner app** (e.g., Fing).
3. Scan for services and verify that `_aura-x._tcp` appears in the list.

**Expected Outcome**:
- ✅ `_aura-x._tcp` service is discovered on the local network.

**Troubleshooting**:
- If **mDNS service is not discovered**, check `adb logcat` for errors in `LANServer.registerMdnsService()`.
- Ensure **both devices are on the same Wi-Fi network**.
- Verify that **mDNS is supported** on your network.

---

### **5.3 Test QR Pairing**
1. Start the LAN Server on **Device A** (as described in Section 5.1).
2. Open the app on **Device A** and navigate to **Settings → Network → LAN Server → QR Pairing**.
3. Tap **Generate QR Code** and verify that a QR code is displayed.
4. On **Device B**, scan the QR code using a **QR scanner app** (or the AURA-X app if QR scanning is implemented).
5. Verify that **Device B** can connect to **Device A** using the IP and port from the QR code.

**Expected Outcome**:
- ✅ QR code is generated and contains the correct IP, port, and auth token.
- ✅ Device B can connect to Device A using the QR code data.

**Troubleshooting**:
- If **QR code is not generated**, check `adb logcat` for errors in `QRPairingManager`.
- Ensure **CAMERA permission** is granted for QR scanning.
- Verify that the **QR code data** is valid (e.g., JSON format).

---

### **5.4 Test Model Inference Over LAN**
1. Start the LAN Server on **Device A** (as described in Section 5.1).
2. On **Device B**, use a **HTTP client** (e.g., `curl` or Postman) to send a request to `http://<DEVICE_A_IP>:8080/infer`.
3. Verify that **Device A** processes the request and returns a response.

**Expected Outcome**:
- ✅ Device A receives and processes the inference request.
- ✅ Device B receives a valid response (e.g., "Inference result: Hello from AURA-X!").

**Troubleshooting**:
- If **request fails**, check `adb logcat` for errors in `LANServer.handleClient()`.
- Ensure **Device B can reach Device A** on the specified port.
- Verify that the **request format** is correct (e.g., `POST /infer`).

---

## **🧪 6. Unit Tests**
### **6.1 Run Unit Tests Locally**
Run the following command to execute all unit tests:
```bash
./gradlew testDebugUnitTest
```
**Expected Outcome**:
- ✅ All unit tests pass.
- ✅ No test failures or errors.

**Troubleshooting**:
- If **tests fail**, check the test logs for errors.
- Ensure **Mockito is properly configured** (dependencies added in `app/build.gradle.kts`).
- Verify that **mock objects are correctly set up** in the test files.

---

### **6.2 Test VisionManager**
1. Open [`VisionManagerTest.kt`](app/src/test/java/com/aurax/operator/ai/vision/VisionManagerTest.kt).
2. Verify that all test cases pass:
   - `test VisionManager with available vision runtime`
   - `test VisionManager with unavailable vision runtime`
   - `test VisionManager isVisionAvailable`
   - `test VisionManager loadVisionModel`
   - `test VisionManager unloadVisionModel`
   - `test VisionManager getVisionStatus`

**Expected Outcome**:
- ✅ All `VisionManagerTest` tests pass.

---

### **6.3 Test WakeWordManager**
1. Open [`WakeWordManagerTest.kt`](app/src/test/java/com/aurax/operator/voice/wakeword/WakeWordManagerTest.kt).
2. Verify that all test cases pass:
   - `test WakeWordManager startListening`
   - `test WakeWordManager stopListening`
   - `test WakeWordManager onDetection callback`
   - `test WakeWordManager isAvailable`
   - `test WakeWordManager updateSettings`

**Expected Outcome**:
- ✅ All `WakeWordManagerTest` tests pass.

---

### **6.4 Test LANServer**
1. Open [`LANServerTest.kt`](app/src/test/java/com/aurax/operator/network/lan/LANServerTest.kt).
2. Verify that all test cases pass:
   - `test LANServer start and stop`
   - `test LANServer getPort`
   - `test LANServer processRequest for models`
   - `test LANServer processRequest for inference`
   - `test LANServer processRequest for unknown`

**Expected Outcome**:
- ✅ All `LANServerTest` tests pass.

---

## **⚡ 7. CI Workflow Testing**
### **7.1 Run CI Workflow Locally**
Simulate the CI workflow by running the following commands:
```bash
# Build
./gradlew clean assembleDebug

# Unit Tests
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug

# Detekt
./gradlew detektAll
```
**Expected Outcome**:
- ✅ All CI steps pass (build, tests, lint, detekt).

**Troubleshooting**:
- If **build fails**, check for compilation or linking errors.
- If **tests fail**, check the test logs for errors.
- If **lint fails**, fix any lint warnings/errors in the code.
- If **detekt fails**, fix any code style issues reported by detekt.

---

### **7.2 Test GitHub Actions Workflow**
1. Push changes to the `nexus-v5-integrations` branch:
   ```bash
   git add .
   git commit -m "Test CI workflow"
   git push origin nexus-v5-integrations
   ```
2. Go to **GitHub Actions** → **Android CI** workflow.
3. Verify that the workflow **runs successfully** for the `nexus-v5-integrations` branch.

**Expected Outcome**:
- ✅ Workflow runs without errors.
- ✅ All steps (build, tests, lint) pass.
- ✅ Debug APK is uploaded as an artifact.

**Troubleshooting**:
- If **workflow fails**, check the logs for errors in each step.
- Ensure **all dependencies are properly configured** in `app/build.gradle.kts`.
- Verify that **all files are committed and pushed** to the branch.

---

## **📊 8. Test Coverage**
### **8.1 Check Test Coverage**
Run the following command to generate a test coverage report:
```bash
./gradlew testDebugUnitTest --coverage
```
**Expected Outcome**:
- ✅ Test coverage report is generated in `app/build/reports/coverage/`.
- ✅ Coverage is **>80%** for new components (VisionManager, WakeWordManager, LANServer).

---

### **8.2 Add Missing Tests**
If test coverage is low for any component:
1. Identify **uncovered methods** in the coverage report.
2. Add **unit tests** for those methods.
3. Re-run the coverage report to verify improvements.

---

## **🔍 9. Debugging Tips**
### **9.1 Logcat for Native Errors**
If the app crashes or behaves unexpectedly, check `adb logcat` for errors:
```bash
adb logcat | grep -i "aura\|llava\|porcupine\|error"
```

---

### **9.2 Logcat for Java/Kotlin Errors**
Check for Java/Kotlin errors in `adb logcat`:
```bash
adb logcat | grep -i "exception\|error\|crash"
```

---

### **9.3 Inspect Database**
Use **Android Studio's Database Inspector** to inspect the Room database:
1. Open **Android Studio**.
2. Run the app on a device/emulator.
3. Open **Database Inspector** and select the AURA-X app.
4. Inspect tables (e.g., `conversations`, `models`, `memories`).

---

### **9.4 Inspect Network Traffic**
Use **Android Studio's Network Inspector** to inspect HTTP requests:
1. Open **Android Studio**.
2. Run the app on a device/emulator.
3. Open **Network Inspector** and start capturing traffic.
4. Perform actions that trigger network requests (e.g., model downloads, LAN Server requests).

---

## **📝 10. Known Issues and Workarounds**
| **Issue**                          | **Workaround**                                                                                     |
|-----------------------------------|---------------------------------------------------------------------------------------------------|
| llava.cpp not integrated           | Use OCR + Accessibility as fallback for vision tasks.                                           |
| Porcupine not integrated          | Use manual voice input (e.g., microphone button) for wake word detection.                      |
| Barge-in not working               | Ensure RECORD_AUDIO permission is granted and microphone is working.                            |
| mDNS not discovered                | Use manual IP:port entry for LAN Server connections.                                            |
| QR code not scanning               | Use a third-party QR scanner app to test QR code generation.                                    |
| Unit tests failing                 | Ensure Mockito dependencies are added and mocks are properly configured.                        |
| Detekt reporting code style issues | Fix code style issues (e.g., naming, complexity) as reported by detekt.                        |

---

## **🎯 11. Final Checklist Before Release**
Before releasing **AURA-X NEXUS v5.0**, verify the following:

### **Build**
- [ ] Debug APK builds successfully (`./gradlew assembleDebug`).
- [ ] Release APK builds successfully (`./gradlew assembleRelease`).
- [ ] All native libraries are properly linked.

### **Tests**
- [ ] All unit tests pass (`./gradlew testDebugUnitTest`).
- [ ] Lint passes (`./gradlew lintDebug`).
- [ ] Detekt passes (`./gradlew detektAll`).
- [ ] Test coverage is >80% for new components.

### **Features**
- [ ] Vision Runtime works (fallback to OCR + Accessibility).
- [ ] Wake Word Detection works (placeholder).
- [ ] Barge-In works (interrupts TTS to listen).
- [ ] LAN Server Mode works (mDNS + QR pairing).
- [ ] Continuous Conversation works (wake word + barge-in).

### **Security**
- [ ] No unnecessary permissions are declared.
- [ ] AccessibilityService does not log sensitive data.
- [ ] Network calls use HTTPS and certificate validation.
- [ ] File access does not use arbitrary paths.
- [ ] Model downloads validate checksums.

### **Documentation**
- [ ] `README.md` is updated with NEXUS v5.0 features.
- [ ] `NEXUS_V5_IMPLEMENTATION.md` is updated with latest progress.
- [ ] `TESTING_GUIDE.md` is complete and accurate.

---

## **🔗 Useful Links**
- **Repository**: [abdulraheemnohri/AURA-X-OPERATOR](https://github.com/abdulraheemnohri/AURA-X-OPERATOR)
- **Branch**: [`nexus-v5-integrations`](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/tree/nexus-v5-integrations)
- **CI Workflows**:
  - [Android CI](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/actions/workflows/android-ci.yml)
  - [Detekt](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/actions/workflows/detekt.yml)
- **External Dependencies**:
  - [llava.cpp](https://github.com/ggml-org/llava.cpp)
  - [Porcupine](https://github.com/Picovoice/porcupine-android)
  - [ZXing](https://github.com/zxing/zxing)
  - [Mockito](https://site.mockito.org/)
  - [Detekt](https://detekt.dev/)

---

## **📧 Contact**
For questions or issues, please open a **GitHub Issue** or **Discussion** in the repository:
- [Issues](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/issues)
- [Discussions](https://github.com/abdulraheemnohri/AURA-X-OPERATOR/discussions)
