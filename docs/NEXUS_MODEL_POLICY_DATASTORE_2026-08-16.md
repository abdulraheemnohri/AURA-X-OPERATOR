# NEXUS Model Download Policy — DataStore Consolidation

Date: 2026-08-16

## Implemented

The model-download policy no longer owns a separate application settings store.

`ModelDownloadSettings` now uses the shared `SettingsDataStoreBackend`, so automatic download, unmetered-only, charging-only, battery threshold, parallel download count, speed limit, automatic retry, and retry count all persist through the application's Preferences DataStore.

## Backward compatibility

The previous `aurax_model_download_policy` SharedPreferences store is migrated once when legacy values exist. Existing values are copied into DataStore and a migration marker prevents repeated migration.

## Runtime behavior

The existing model downloader and automatic-download scheduler continue to consume the typed `ModelDownloadSettings` API. This keeps callers stable while removing the duplicate persistence implementation.

## Truthfulness rule

This change affects persistence only. It does not make any model runtime READY. Runtime availability must still be determined by actual installed model files and a working runtime.

## Verification gate

CI must compile the DataStore-backed implementation and execute the model-policy/unit test suite before this pass is considered green.
