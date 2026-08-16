# AURA-X NEXUS — Parallel Model Download Pass

Date: 2026-08-16

## Implemented

The Model Hub's `maximumParallelDownloads` policy is now enforced at runtime.

Previously, the setting existed in the download policy but WorkManager could run multiple uniquely named model jobs concurrently without a process-level transfer limit.

The new `ModelDownloadConcurrencyGate`:

- enforces a maximum of 1 or 2 active model transfers;
- reads the configured limit when a worker acquires a permit;
- queues excess transfers without starting another HTTP transfer;
- remains coroutine-cancellation aware;
- releases the permit in a `finally` block;
- exposes the active-transfer count for diagnostics/tests.

`ModelDownloadWorker` now wraps the real downloader with this gate.

## Cancellation behavior

Coroutine cancellation is intentionally allowed to propagate. The worker must not convert WorkManager cancellation into an ordinary failure result, because doing so can interfere with correct cancellation semantics and retry behavior.

## Verification

A unit test covers four concurrent transfer requests with a configured maximum of two and asserts that the observed peak concurrency never exceeds two.

## Remaining model-system work

- migrate download policy persistence from SharedPreferences to DataStore;
- expose active/queued transfer counts in Model Hub diagnostics;
- add persistent queue ordering/cancellation semantics if the product requires downloads to survive process death as a coordinated queue;
- continue native runtime integration and capability truthfulness.

## Release rule

A configured download limit is considered implemented only when it constrains the actual transfer execution path, not merely when a preference or UI control exists.
