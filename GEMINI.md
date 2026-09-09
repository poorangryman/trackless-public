# TrackLess Repository Guidelines & Mandatory Rules

## 🚨 MANDATORY RELEASE VERSIONING POLICY (STRICT)

Whenever ANY change is made in this repository (code modification, UI update, feature addition, or bug fix):
1. **Never release or commit with a previously used version number.**
2. **Always increment the version (patch bump by default)**:
   - Update `VERSION.txt` with the new version (e.g. `1.3.1` -> `1.3.2`).
   - `app/build.gradle`:
     - `versionCode` is computed automatically from `tracklessVersionName` (`major * 10000 + minor * 100 + patch`), ensuring an strictly increasing versionCode for Android package management.
     - `versionName` automatically reads from `VERSION.txt`.
3. **Always build release APK before committing**:
   - Run: `./gradlew assembleRelease`
   - Verify that `app/build/outputs/apk/release/TrackLess-vX.Y.Z.apk` is generated.
4. **Git Commit, Tag & Push**:
   - Commit with the new version in the commit message: `feat: ... (vX.Y.Z)`.
   - Create an annotated or lightweight Git tag: `git tag vX.Y.Z`.
   - Push both the branch and the tag to GitHub: `git push origin main --tags`.
   - This triggers the GitHub Actions workflow (`.github/workflows/build-apk.yml`), which automatically creates a GitHub Release with tag `vX.Y.Z` and attaches `TrackLess-vX.Y.Z.apk`.
   - This ensures the user NEVER downloads an APK with the same name.

## 📱 UI/UX Standards
- **No native browser alerts (`alert()`, `confirm()`)**: Use inline liquid glass cards, toasts, or modals with haptic feedback.
- **No emojis in core controls or lists**: Use minimalist stroked SVG icons (`stroke="currentColor"`, `fill="none"`).
