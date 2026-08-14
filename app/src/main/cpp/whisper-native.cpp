#include <jni.h>
#include <android/log.h>
#include "whisper.h"
#include <algorithm>
#include <string>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AURA-X-Whisper", __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_aurax_operator_voice_stt_WhisperRecognizer_nativeTranscribe(
    JNIEnv* env,
    jobject,
    jstring modelPath,
    jfloatArray pcm,
    jstring language,
    jint threads) {
    if (!modelPath || !pcm) {
        return env->NewStringUTF("");
    }

    const char* model = env->GetStringUTFChars(modelPath, nullptr);
    const char* lang = language ? env->GetStringUTFChars(language, nullptr) : nullptr;
    const jsize count = env->GetArrayLength(pcm);
    jfloat* samples = env->GetFloatArrayElements(pcm, nullptr);

    if (!model || !samples || count <= 0) {
        if (samples) env->ReleaseFloatArrayElements(pcm, samples, JNI_ABORT);
        if (lang) env->ReleaseStringUTFChars(language, lang);
        if (model) env->ReleaseStringUTFChars(modelPath, model);
        return env->NewStringUTF("");
    }

    whisper_context_params contextParams = whisper_context_default_params();
    contextParams.use_gpu = false;
    contextParams.flash_attn = false;

    whisper_context* ctx = whisper_init_from_file_with_params(model, contextParams);
    if (!ctx) {
        LOGE("Unable to load Whisper model: %s", model);
        env->ReleaseFloatArrayElements(pcm, samples, JNI_ABORT);
        if (lang) env->ReleaseStringUTFChars(language, lang);
        env->ReleaseStringUTFChars(modelPath, model);
        return env->NewStringUTF("");
    }

    const std::string requestedLanguage = (lang && *lang) ? lang : "auto";
    whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.no_timestamps = true;
    params.translate = false;
    params.language = requestedLanguage.c_str();
    params.detect_language = requestedLanguage == "auto";
    params.n_threads = std::clamp(static_cast<int>(threads), 1, 8);
    params.n_max_text_ctx = 0;
    params.temperature = 0.0f;
    params.temperature_inc = 0.0f;

    const int rc = whisper_full(ctx, params, samples, count);
    std::string result;
    if (rc == 0) {
        const int segments = whisper_full_n_segments(ctx);
        for (int i = 0; i < segments; ++i) {
            const char* text = whisper_full_get_segment_text(ctx, i);
            if (!text) continue;
            if (!result.empty()) result.push_back(' ');
            result.append(text);
        }
    } else {
        LOGE("Whisper inference failed: %d", rc);
    }

    whisper_free(ctx);
    env->ReleaseFloatArrayElements(pcm, samples, JNI_ABORT);
    if (lang) env->ReleaseStringUTFChars(language, lang);
    env->ReleaseStringUTFChars(modelPath, model);

    return env->NewStringUTF(result.c_str());
}
