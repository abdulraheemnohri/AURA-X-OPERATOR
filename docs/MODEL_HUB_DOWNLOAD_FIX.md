# Model Hub Download Fix

The Model Hub download flow now keeps an existing WorkManager job instead of replacing it on repeated button taps, exposes Cancel while downloading, and exposes Retry Download after an error.

The downloader validates resumed HTTP `206` responses with `Content-Range`, preserves coroutine cancellation, verifies the built-in Qwen model against its pinned 491 MB size and SHA-256, and reports progress through the Room model registry.

The primary Qwen asset is the public `Qwen/Qwen2.5-0.5B-Instruct-GGUF` Q4_K_M file. Hugging Face currently lists it as a 491 MB Xet file with SHA-256 `74a4da8c9fdbcd15bd1f6d01d621410d31c6fc00986f5eb687824e7b93d7a9db`.
