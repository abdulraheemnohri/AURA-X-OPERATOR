#include <jni.h>
#include <android/log.h>
#include "llama.h"

#include <algorithm>
#include <mutex>
#include <string>
#include <vector>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "AURA-X", __VA_ARGS__)

namespace {
std::mutex g_mutex;
llama_model * g_model = nullptr;
std::string g_model_path;

void unload_model() {
    if (g_model) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    g_model_path.clear();
}

bool ensure_model(const std::string & path) {
    if (g_model && g_model_path == path) return true;
    unload_model();

    llama_model_params params = llama_model_default_params();
    params.n_gpu_layers = 0; // CPU-first and broadly compatible on Android.
    g_model = llama_model_load_from_file(path.c_str(), params);
    if (!g_model) {
        LOGE("Unable to load GGUF model: %s", path.c_str());
        return false;
    }
    g_model_path = path;
    return true;
}
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_aurax_operator_ai_runtime_LlamaCppRuntime_nativeGenerate(
        JNIEnv * env, jobject,
        jstring modelPath,
        jstring prompt,
        jint maxTokens) {
    const char * model_chars = env->GetStringUTFChars(modelPath, nullptr);
    const char * prompt_chars = env->GetStringUTFChars(prompt, nullptr);
    std::string model_path(model_chars ? model_chars : "");
    std::string input(prompt_chars ? prompt_chars : "");
    env->ReleaseStringUTFChars(modelPath, model_chars);
    env->ReleaseStringUTFChars(prompt, prompt_chars);

    std::lock_guard<std::mutex> lock(g_mutex);
    if (model_path.empty() || input.empty() || !ensure_model(model_path)) {
        return env->NewStringUTF("AURA-X: unable to load local model.");
    }

    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    const int n_prompt = -llama_tokenize(vocab, input.c_str(), input.size(), nullptr, 0, true, true);
    if (n_prompt <= 0) return env->NewStringUTF("AURA-X: prompt tokenization failed.");

    std::vector<llama_token> prompt_tokens(n_prompt);
    if (llama_tokenize(vocab, input.c_str(), input.size(), prompt_tokens.data(), prompt_tokens.size(), true, true) < 0) {
        return env->NewStringUTF("AURA-X: prompt tokenization failed.");
    }

    llama_context_params ctx_params = llama_context_default_params();
    const int requested = std::max(32, static_cast<int>(maxTokens));
    ctx_params.n_ctx = std::min<uint32_t>(2048, static_cast<uint32_t>(n_prompt + requested + 8));
    ctx_params.n_batch = std::min<uint32_t>(512, ctx_params.n_ctx);
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;

    llama_context * ctx = llama_init_from_model(g_model, ctx_params);
    if (!ctx) return env->NewStringUTF("AURA-X: context creation failed.");

    llama_sampler_chain_params sampler_params = llama_sampler_chain_default_params();
    llama_sampler * sampler = llama_sampler_chain_init(sampler_params);
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.2f));
    llama_sampler_chain_add(sampler, llama_sampler_init_greedy());

    llama_batch batch = llama_batch_get_one(prompt_tokens.data(), prompt_tokens.size());
    std::string output;
    output.reserve(static_cast<size_t>(requested) * 4);

    for (int generated = 0; generated < requested; ++generated) {
        if (llama_decode(ctx, batch) != 0) {
            output = "AURA-X: inference decode failed.";
            break;
        }

        const llama_token token = llama_sampler_sample(sampler, ctx, -1);
        if (llama_vocab_is_eog(vocab, token)) break;

        char piece[512];
        const int n = llama_token_to_piece(vocab, token, piece, sizeof(piece), 0, true);
        if (n > 0) output.append(piece, n);
        
        // Create a const vector to pass to llama_batch_get_one
        const std::vector<llama_token> token_vec = {token};
        batch = llama_batch_get_one(token_vec.data(), 1);
    }

    llama_sampler_free(sampler);
    llama_free(ctx);
    return env->NewStringUTF(output.c_str());
}
