---
trigger: always_on
description: Strict versioning rule for TrackLess: any commit/build/change must bump the version and produce a new release tag and APK
---

# Mandatory Release Versioning Rule for TrackLess

Every time ANY change (visual, feature, bugfix, refactoring) is made in the TrackLess repository:

1. **Increment the semantic version**:
   - `VERSION.txt`: increment patch version (e.g. `1.3.1` -> `1.3.2`).
   - `app/build.gradle`:
     - `versionCode` is computed automatically from `tracklessVersionName` via `major * 10000 + minor * 100 + patch`.
     - `versionName` automatically reads from `VERSION.txt`.
   - `app/src/main/assets/index.html`:
     - Update version string in `#settings-about`: `TrackLess vX.Y.Z · Liquid Glass Edition`.

2. **Assemble release APK**:
   - Run: `.\gradlew.bat assembleRelease` (using JDK 17).
   - Verify `TrackLess-vX.Y.Z.apk` is generated in `app/build/outputs/apk/release/`.

3. **Git Commit & Tag**:
   - Always commit with version in message: `git commit -m "... (vX.Y.Z)"`.
   - Tag the commit: `git tag vX.Y.Z`.
   - Push with tags: `git push origin main --tags`.
   - GitHub Actions will build and create a GitHub Release with the uniquely named APK download (`TrackLess-vX.Y.Z.apk`).
