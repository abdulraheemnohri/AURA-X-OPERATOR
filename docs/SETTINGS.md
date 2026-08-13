# AURA-X Settings

## Persistence

AURA-X has two local settings layers:

1. `SecurePrefs` for security/operator controls and selected model paths. Values are stored using Android encrypted shared preferences.
2. `SettingsRepository` for the broader runtime configuration surface: inference, voice, memory, privacy, appearance, performance, LAN and debugging options.

Settings are local-only and are not uploaded to Hugging Face.

## Save / apply contract

The settings UX should expose an explicit **Save Settings** action even though individual secure preference setters persist immediately. Save is the user-facing commit point: it confirms that the current screen state has been accepted and applied to the runtime. Runtime consumers must re-read persisted values rather than retaining stale copies.

Recommended UX:

- show an unsaved-change indicator when editable draft values differ from persisted values
- `Save Settings` applies the draft and clears the indicator
- `Discard` restores persisted values
- `Reset section` restores safe defaults for the current section
- `Reset all` requires confirmation

## Safety defaults

Recommended defaults remain conservative:

- Confirm Actions automation policy
- biometric lock enabled
- floating operator indicator enabled
- incognito protection enabled
- password filtering enabled
- screenshot verification enabled
- screen-change abort enabled
- local-only mode enabled
- thermal protection enabled
- automatic model unload enabled

## Model settings

Model settings include active model selection, context length, temperature, top-p, top-k, maximum output tokens, CPU threads, GPU acceleration and batch size. Values are range-checked before persistence.

## Recovery

If a setting causes a bad runtime state, reset the affected section first. For model issues, use Model Hub integrity verification and unload the active model before deleting it.

## Privacy

Typed/password-like values must never be copied into safety logs. Operator activity metadata can remain local for diagnostics and auditability. Exported safety logs are intended for local troubleshooting.
