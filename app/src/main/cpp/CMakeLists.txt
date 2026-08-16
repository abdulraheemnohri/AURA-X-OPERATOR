cmake_minimum_required(VERSION 3.22.1)
project(aurax_native LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

if(ANDROID AND NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "AURA-X native runtime supports arm64-v8a only; unsupported ABI: ${ANDROID_ABI}")
endif()

# Android-friendly CPU-only builds for both local LLM and offline STT.
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(WHISPER_CURL OFF CACHE BOOL "" FORCE)
set(WHISPER_SDL2 OFF CACHE BOOL "" FORCE)
set(WHISPER_OPENVINO OFF CACHE BOOL "" FORCE)
set(WHISPER_COREML OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "" FORCE)
set(GGML_RPC OFF CACHE BOOL "" FORCE)

include(FetchContent)

FetchContent_Declare(
    llama_cpp
    GIT_REPOSITORY https://github.com/ggml-org/llama.cpp.git
    GIT_TAG b10087
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(llama_cpp)

FetchContent_Declare(
    whisper_cpp
    GIT_REPOSITORY https://github.com/ggml-org/whisper.cpp.git
    GIT_TAG v1.9.1
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(whisper_cpp)

# LLaVA remains intentionally gated until a compatible Android native runtime
# and model-loading path are validated. The Kotlin capability layer must not
# report Vision READY merely because this JNI library exists.

# Native LLM bridge. Link the FetchContent target instead of assuming a
# generated libllama.a location; this is portable across llama.cpp layouts.
add_library(llama-lib STATIC native-lib.cpp)
target_link_libraries(llama-lib PRIVATE llama log android)

# Native Whisper bridge.
add_library(whisper-lib STATIC whisper-native.cpp)
target_link_libraries(whisper-lib PRIVATE whisper log android)

# Vision JNI boundary. This library intentionally reports unavailable until
# a real multimodal runtime is linked and successfully loads a model.
add_library(aurax_llava SHARED llava-native.cpp)
target_link_libraries(aurax_llava PRIVATE log android)

# Main JNI/shared library. The anchor source gives CMake a concrete source
# file; linking static bridges through this target keeps the Android packaging
# contract explicit instead of relying on a source-less shared target.
add_library(aurax_native SHARED aurax-native-anchor.cpp)
target_link_libraries(aurax_native PRIVATE llama-lib whisper-lib log android)
