# Release Gate

Do not merge `fix/model-hub-download-settings` into `main` until the following are green:

- [ ] Android debug APK builds with Gradle 8.2 / JDK 17.
- [ ] Unit tests pass.
- [ ] CMake + NDK 25.2.9519653 native build passes.
- [ ] Qwen GGUF direct asset downloads successfully.
- [ ] Interrupted download resumes without corrupting the file.
- [ ] SHA-256 verification rejects a modified file when a hash is supplied.
- [ ] A downloaded model reaches READY only with an existing local file.
- [ ] Load/unload works with the native runtime.
- [ ] Settings survive process restart.
- [ ] Accessibility, overlay, notification and microphone permission flows work.
- [ ] Emergency abort remains reachable while automation is active.
- [ ] Android 9+ arm64 smoke test passes.

This gate is intentionally conservative: source review is not a substitute for a real Android/native build.
