#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_aurax_operator_ai_runtime_LlamaCppRuntime_nativeGenerate(JNIEnv* env, jobject, jstring prompt) {
    const char* p = env->GetStringUTFChars(prompt, nullptr);
    std::string response =
        "AURA-X native bridge is loaded, but llama.cpp inference is intentionally not bundled in this source tree. "
        "Install the pinned llama.cpp backend and GGUF model described in MODEL_SETUP.md. Prompt: ";
    response += p;
    env->ReleaseStringUTFChars(prompt, p);
    return env->NewStringUTF(response.c_str());
}
