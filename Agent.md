# AGENTS Guidelines for This Repository

This repository contains the active TrackLess Android application. When working on the project interactively with an AI coding agent, follow the guidelines below to preserve the current architecture and minimize regression risk.

## 1. Project Specifications
- **Minimum SDK:** 23
- **Target SDK:** 35
- **Compile SDK:** 35
- **Language:** Kotlin
- **Build System:** Gradle Groovy DSL (`build.gradle`)
- **UI:** Jetpack Compose
- **Widgets:** Android Glance AppWidgets
- **Local persistence:** Jetpack DataStore Preferences
- **Application ID:** `ru.otvykaniye.tracker`

The live repository is authoritative if this document and the source disagree.

## 2. Architecture & Design Patterns
The active application uses a small Compose + ViewModel + Repository architecture:
- **Presentation Layer:** `TracklessViewModel` exposes `StateFlow<TracklessState>` to Compose UI.
- **UI Layer:** Jetpack Compose under `ui/`.
- **Data Layer:** `TracklessRepository` backed by DataStore Preferences.
- **State model:** `TracklessState`, `ProfileState`, and `ConsumptionEntry` in `Models.kt`.
- **Widgets:** Glance widgets share the persisted application state.

Do not introduce Room, Retrofit, Hilt/Dagger, or another large architectural dependency unless the task explicitly requires it. Prefer the existing lightweight architecture.

## 3. Asynchronous Programming
- Use Kotlin Coroutines and Flow.
- Do not use `runBlocking` on application/UI initialization paths.
- Serialize consecutive state mutations so rapid user actions cannot overwrite newer state with an older snapshot.
- Keep slow widget refresh work outside the state mutation critical section.
- Avoid blocking the main thread for DataStore reads or writes.

## 4. UI Framework
- Jetpack Compose is the active UI framework.
- Follow unidirectional data flow: UI events -> ViewModel -> state -> UI.
- Keep composables free of blocking work and uncontrolled side effects.
- Reuse the existing localization and theme infrastructure rather than adding parallel systems.

## 5. Testing Philosophy
- Prefer unit tests for pure calculations and ViewModel state transitions.
- Test persistence behavior with rapid consecutive updates where practical.
- Test calendar-day boundaries explicitly, including midnight and timezone/DST transitions.
- When widgets or import/export are changed, verify those paths separately.

## 6. Data & Compatibility
- Preserve existing JSON state fields unless a migration is intentionally implemented.
- Import/export must preserve existing user data.
- Do not silently discard unknown or future-compatible state fields.
- Keep source files UTF-8 without BOM.

## 7. Release & CI
- `VERSION.txt` is the source for the release version name and generated versionCode.
- Keep `VERSION.txt`, `app/build.gradle`, and `README.md` consistent when making a release version change.
- Never commit keystores, passwords, tokens, or other secrets.
- GitHub Actions is responsible for release APK builds and GitHub Releases from `main`.
- Release/build workflow runs must be checked after build-related changes.

## 8. External Documentation
When implementing behavior that depends on Android APIs or platform-specific details, prefer official Android and Kotlin documentation.

## 9. Useful Agent Skills Recap

| Skill Folder | Purpose |
| --- | --- |
| `architecture/` | ViewModels, state, repositories, and data-layer design. |
| `ui/` | Jetpack Compose practices, accessibility, and UI structure. |
| `performance/` | Compose and Gradle performance auditing. |
| `migration/` | Architecture and framework migrations. |
| `testing_and_automation/` | Unit/UI testing and emulator automation. |
| `concurrency_and_networking/` | Coroutine concurrency and networking fixes. |

When a task is specialized, inspect the corresponding skill before making changes.
