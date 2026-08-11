cmake_minimum_required(VERSION 3.22.1)
project(aurax_native)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# AURA-X native runtime is intentionally arm64-only. llama.cpp/ggml currently
# hits an ARM32 half-precision intrinsic issue (vld1q_f16/vld1_f16) with the
# Android NDK used by CI. Keeping the unsupported ABI out of the build prevents
# a guaranteed CI failure while preserving the supported arm64-v8a runtime.
if(ANDROID AND NOT ANDROID_ABI STREQUAL "arm64-v8a")
    message(FATAL_ERROR "AURA-X native runtime supports arm64-v8a only; unsupported ABI: ${ANDROID_ABI}")
endif()

set(LLAMA_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP OFF CACHE BOOL "" FORCE)

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
