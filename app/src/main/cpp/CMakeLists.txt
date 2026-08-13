cmake_minimum_required(VERSION 3.22.1)
project(aurax_native LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)

# AURA-X ships a single native ABI. The Android app declares arm64-v8a,
# so fail early for accidental unsupported native builds instead of producing
# a partially configured runtime.
if(ANDROID AND NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "AURA-X native runtime supports arm64-v8a only; unsupported ABI: ${ANDROID_ABI}")
endif()

# Keep the embedded runtime CPU-only and Android-friendly. In particular,
# llamafile is not supported on Android and OpenMP is intentionally disabled
# because Android's NDK toolchain does not provide a portable OpenMP dependency.
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_TOOLS OFF CACHE BOOL "" FORCE)
set(LLAMA_OPENSSL OFF CACHE BOOL "" FORCE)
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

add_library(aurax_native SHARED native-lib.cpp)
target_include_directories(aurax_native PRIVATE ${llama_cpp_SOURCE_DIR}/include)
target_link_libraries(aurax_native PRIVATE llama log android)
