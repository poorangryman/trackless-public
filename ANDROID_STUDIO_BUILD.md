# TrackLess 1.2.4 — Android Studio build guide

## 1. Open

Open the project folder containing `settings.gradle`.

Expected project files:

- `settings.gradle`
- `build.gradle`
- `gradle/wrapper/gradle-wrapper.properties`
- `app/build.gradle`
- `app/src/main/...`

Android Studio should use the Gradle wrapper supplied with the project.

## 2. Check the toolchain

- Android Gradle Plugin: **8.13.2**
- Gradle: **8.13**
- JDK: **17**
- Compile SDK: **35**
- Target SDK: **35**
- Minimum SDK: **23**

If Android Studio asks to change these versions, do not upgrade them just to make Sync work. First set Gradle JDK to JDK 17 and let the supplied versions sync.

## 3. Test the project

Run:

**Build → Make Project**

Then:

**Build → Build Bundle(s) / APK(s) → Build APK(s)**

## 4. Generate the final release APK

Choose:

**Build → Generate Signed App Bundle / APK**

Then:

1. Select **APK**.
2. Select **release**.
3. Select the existing TrackLess keystore if you need update compatibility with an APK previously signed by this project.
4. Finish the wizard.
5. Open `app/build/outputs/apk/release/`.

The Gradle build also creates:

`TrackLess-v1.2.4.apk`

## 5. If Sync fails

Do not delete `app/src` or replace the project with a new Android project.

Instead:

1. Open **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**.
2. Set **Gradle JDK** to JDK 17.
3. Make sure the project uses the **Gradle wrapper**.
4. Run **File → Sync Project with Gradle Files**.

## 6. If the app opens but looks blank

The HTML app is stored at:

`app/src/main/assets/index.html`

The Android wrapper loads it through:

`file:///android_asset/index.html`

Do not move that file to `res/` or rename the `assets` directory.
