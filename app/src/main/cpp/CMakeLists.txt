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

# Fetch llava.cpp for vision-language models
# Note: llava.cpp is still evolving, so this is commented out for now.
# Uncomment once llava.cpp stabilizes and is ready for Android.
# FetchContent_Declare(
#     llava_cpp
#     GIT_REPOSITORY https://github.com/ggml-org/llava.cpp.git
#     GIT_TAG main
#     GIT_SHALLOW TRUE
# )
# FetchContent_MakeAvailable(llava_cpp)

# Build llama.cpp as a static library
add_library(
    llama-lib
    STATIC
    native-lib.cpp
)

target_link_libraries(
    llama-lib
    ${llama_cpp_SOURCE_DIR}/libllama.a
    log
    android
)

# Build whisper-native as a static library
add_library(
    whisper-lib
    STATIC
    whisper-native.cpp
)

target_link_libraries(
    whisper-lib
    ${whisper_cpp_SOURCE_DIR}/libwhisper.a
    log
    android
)

# Build llava-native as a shared library
# Note: This is a placeholder for now. Once llava.cpp is integrated,
# we will link against the actual llava library.
add_library(
    aurax_llava
    SHARED
    llava-native.cpp
)

target_link_libraries(
    aurax_llava
    log
    android
    # Uncomment once llava.cpp is integrated:
    # ${llava_cpp_SOURCE_DIR}/libllava.a
)

# Build the main native library
add_library(
    aurax_native
    SHARED
    # No source files here; we link the static libraries
)

target_link_libraries(
    aurax_native
    llama-lib
    whisper-lib
    log
    android
)
