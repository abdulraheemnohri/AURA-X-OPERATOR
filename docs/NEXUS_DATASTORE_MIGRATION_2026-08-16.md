# NEXUS DataStore Migration — 2026-08-16

## Status
Implemented in `main`.

## What changed

- Added `SettingsDataStoreBackend` using Preferences DataStore.
- Added automatic `SharedPreferencesMigration` from the legacy `aura_settings_v3` store.
- Replaced the application `SettingsRepository` persistence backend with DataStore.
- Preserved the existing typed settings facade and validation ranges so existing callers do not need an architectural rewrite.
- Writes update the in-memory cache immediately and persist asynchronously to DataStore.
- Legacy settings are migrated by AndroidX DataStore on first access.

## Compatibility

The migration keeps the existing setting keys unchanged. This avoids losing user configuration during the transition and preserves the existing default values.

## Verification requirement

The next CI run must verify Kotlin compilation and Detekt. A successful source-level migration is not treated as build verification until GitHub Actions reports success.

## Remaining settings work

- Move model-download-specific preferences to the same DataStore namespace.
- Expose a unified `SettingsSnapshot`/Flow for reactive screens.
- Add instrumentation coverage for first-run migration and restart persistence.
- Remove the legacy SharedPreferences file after migration is proven safe in production releases.
