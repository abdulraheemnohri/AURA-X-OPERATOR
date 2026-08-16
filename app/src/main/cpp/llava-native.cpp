// AURA-X vision JNI boundary.
//
// The repository deliberately does NOT pretend to contain a working llava.cpp
// runtime. Until the actual llava.cpp sources are linked into CMake, this
// boundary reports the runtime as unavailable instead of returning fabricated
// vision results. This keeps capability state truthful and prevents the
// operator from acting on invented screen understanding.

#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "LlavaNative"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static bool g_loaded = false;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_aurax_operator_ai_vision_LlavaVisionRuntime_nativeLoad(
    JNIEnv* env,
    jobject /*thiz*/,
    jstring modelPath
) {
    const char* path = modelPath ? env->GetStringUTFChars(modelPath, nullptr) : nullptr;
    LOGE("llava.cpp runtime is not bundled; refusing to report a model as loaded: %s", path ? path : "<null>");
    if (path) env->ReleaseStringUTFChars(modelPath, path);
    g_loaded = false;
    return JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_aurax_operator_ai_vision_LlavaVisionRuntime_nativeUnload(
    JNIEnv* /*env*/,
    jobject /*thiz*/
) {
    g_loaded = false;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_aurax_operator_ai_vision_LlavaVisionRuntime_nativeAnalyze(
    JNIEnv* env,
    jobject /*thiz*/,
    jobject /*bitmap*/,
    jstring /*prompt*/
) {
    if (!g_loaded) {
        LOGE("Vision analysis requested while llava.cpp runtime is unavailable");
        return nullptr;
    }
    return nullptr;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_aurax_operator_ai_vision_LlavaVisionRuntime_nativeGetStatus(
    JNIEnv* /*env*/,
    jobject /*thiz*/
) {
    return g_loaded ? 2 : 0; // READY only after a real runtime has been linked and loaded.
}
