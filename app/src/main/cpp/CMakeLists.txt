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
FetchContent_Declare(
    llava_cpp
    GIT_REPOSITORY https://github.com/ggml-org/llava.cpp.git
    GIT_TAG main
    GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(llava_cpp)

add_library(aurax_native SHARED native-lib.cpp)
target_include_directories(aurax_native PRIVATE ${llama_cpp_SOURCE_DIR}/include)
target_link_libraries(aurax_native PRIVATE llama log android)

add_library(aurax_whisper SHARED whisper-native.cpp)
target_include_directories(aurax_whisper PRIVATE
    ${whisper_cpp_SOURCE_DIR}/include
    ${whisper_cpp_SOURCE_DIR}/ggml/include
)
target_link_libraries(aurax_whisper PRIVATE whisper log android)

# Build llava-native as a shared library
add_library(aurax_llava SHARED llava-native.cpp)
target_include_directories(aurax_llava PRIVATE
    ${llava_cpp_SOURCE_DIR}/include
    ${llava_cpp_SOURCE_DIR}/ggml/include
)
target_link_libraries(aurax_llava PRIVATE llava log android)
