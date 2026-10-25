# Play Policy Notes — FocusBlock (`play` flavor)

## Permissions used

| Permission | Declared in | Justification |
|------------|-------------|---------------|
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | Manifest (granted by system when user enables the service) | Required to bind the AccessibilityService that detects foreground app changes and enforces blocks. The service is exported and declared in the manifest. |
| `android.permission.FOREGROUND_SERVICE` | Manifest | Allows the accessibility/blocking service to run as a foreground service so it is not killed by the system. |
| `android.permission.POST_NOTIFICATIONS` | Manifest | Shows the persistent notification with active blocks and time remaining. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Manifest | Re-initializes block state after device reboot. |
| `android.permission.WAKE_LOCK` | Manifest | Briefly holds a wake lock to show the block screen and process time changes. |

### Not declared (intentionally)
- **No `INTERNET` permission.** All data is local; nothing is collected or transmitted.
- **No `QUERY_ALL_PACKAGES`.** Launchable apps are enumerated via a `<queries>` intent filter (`ACTION.MAIN` + `category.LAUNCHER`), which is sufficient for listing user-facing apps.
- **No `READ_PHONE_STATE`, `READ_CALL_LOG`, etc.**

## Accessibility Service policy considerations

- The service requests `android.accessibilityservice.AccessibilityService` and handles `TYPE_WINDOW_STATE_CHANGED`.
- `canRetrieveWindowContent = false` — no screen content is read.
- The service only reads the foreground package name; it does not record keystrokes or window content.
- In-app disclosure (Play flavor only) states exactly what the service is used for and that no screen content is collected or transmitted.
- Users can disable the service at any time in system Settings.

## Device Admin policy considerations

- `FocusBlockDeviceAdminReceiver` is declared with `android.permission.BIND_DEVICE_ADMIN` and `android.app.action.DEVICE_ADMIN_*` intent filters.
- `onDisableRequested` returns a clear warning message explaining that disabling admin allows uninstallation and bypasses strict blocks.
- Device Admin is only used to prevent uninstallation while a Strict Mode block is active. It does not enforce password policies, wipe data, or lock the device.
- In-app disclosure (Play flavor only) states the purpose and that consent can be revoked.

## Risks to verify against current Google Play policy

1. **Accessibility Services policy** — Google requires accessibility services to be used only for accessibility-related purposes unless a strong justification exists. App blockers are a common use case but have been scrutinized. Verify that the in-app disclosure is prominent before requesting the service and that the service description is accurate.
2. **Device Admin policy** — Device Admin is permitted for enterprise/MDM use; using it solely to prevent self-uninstallation may be acceptable but should be clearly disclosed.
3. **"Undetectable" or "stealth" behavior** — The app must not hide its icon, disguise itself, or prevent the user from easily removing it outside strict mode. The block screen is full-screen but clearly labeled and provides a "Go home" button.
4. **Foreground service** — Must show a notification and have a valid use case (blocking reliability). Verify the foreground service type and notification behavior match Play requirements for targetSdk 36.
5. **Full-screen intent / block screen** — The block screen uses `singleInstance` launch mode and `taskAffinity=""`. Ensure this does not interfere with system dialogs or create an unfair obstacle to leaving the app.

## Flavor differences

- `sideload`: All features, no mandatory disclosure screen (but permissions still explained in onboarding).
- `play`: Same code, plus a mandatory consent screen before enabling Accessibility Service and Device Admin (see `PlayDisclosureScreen`).
