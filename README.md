# FocusBlock

Android app blocker with scheduled blocks and Strict Mode.

## Tech stack
- Kotlin, Jetpack Compose, Material 3, single-activity with Navigation Compose
- MVVM + Clean-ish layering (ui / domain / data), Hilt for DI
- Room (profiles, schedules, block log), DataStore (settings), Kotlin Coroutines/Flow
- minSdk 26, targetSdk 36
- Gradle Kotlin DSL, version catalog
- Two product flavors: `sideload` (all features) and `play` (Play-policy-compliant disclosures)

## Core concepts
- **Profile**: name, blocked apps (package names), schedule(s), strict flag, enabled flag.
- **Schedule types**:
  - **Recurring / Time Window**: days of week + start/end time, supports overnight/midnight crossing.
  - **Hourly**: hourly quota window (resets at the top of each hour `:00`).
  - **Daily**: full-day quota window (resets every midnight `00:00`).
  - **One-off**: start/end date-time window.
  - **Quick block**: instant countdown timer.
- **Blocking modes (v3)**:
  - **Static Block**: immediately blocks targeted apps for the entire duration of the schedule.
  - **Usage Limit (App Quota)**: allows usage of targeted apps up to a configured duration limit (e.g., 15 minutes), after which apps are dynamically blocked until the evaluation window ends/resets. Configurable as Per-App or Combined profile budget.
- **Strict Mode**: once active, the profile cannot be deactivated, edited, shortened, or deleted until its end time. Adding apps or extending the end time is allowed.

## Strict Mode enforcement (v1)
- Device Administrator (`DeviceAdminReceiver`) prevents normal uninstallation while active.
- AccessibilityService detects blocked apps and shows the block screen.
- Two strict levels: Targeted (blocks admin/accessibility/app-info/installer screens) and Aggressive (blocks whole Settings app).
- Clock tampering protected via `TrustedClock` anchored to `elapsedRealtime()`, protecting both independent strict mode and active profile schedules against device clock manipulation.
- Emergency unlock (off by default) requires an elapsed-time waiting period.

## Limits (documented in-app)
Strict mode cannot stop Safe Mode, ADB, factory reset, or a second device.

## Build instructions

### Prerequisites
- JDK 17 (`JAVA_HOME` set)
- Android SDK (`ANDROID_HOME` set) with:
  - `platform-tools`
  - `build-tools;34.0.0`
  - `platforms;android-36`
  - `cmdline-tools;latest`

### Build
```bash
# Debug build (sideload flavor)
./gradlew app:assembleSideloadDebug

# Release build (play flavor)
./gradlew app:assemblePlayRelease
```

Signed release:
```bash
./gradlew app:assemblePlayRelease -Pandroid.signing.keyStore=... -Pandroid.signing.keyAlias=...
```

### Test
```bash
# Test Play flavor unit tests
./gradlew testPlayDebugUnitTest

# Test Sideload flavor unit tests
./gradlew testSideloadDebugUnitTest
```

## Known limitations
- Strict mode cannot stop Safe Mode, ADB, factory reset, or a second device.
- Device Admin (non-Device-Owner) cannot fully block `setPackagesSuspended`; those are reserved for Device Owner.
- Accessibility-based blocking requires the user to enable the Accessibility Service in Android Settings.

## QA checklist
See `QA_CHECKLIST.md`.
