# Model Hub Download Debugging

The Model Hub download action uses WorkManager and the `ModelDownloadWorker` to run downloads in the background. A failed download is represented as `ERROR` and the UI must expose Retry rather than leaving the model without an action.

The download pipeline validates:

- source URL
- network policy
- available storage
- HTTP status
- resume `Content-Range`
- expected file size
- GGUF signature
- SHA-256 when available
- final local file

If a download fails, the UI should expose the actionable error and a Retry action. Cancellation must not be converted into a permanent `ERROR` state.
