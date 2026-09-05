# TrackLess ProGuard / R8 rules
# ────────────────────────────────────────────────────────────

# Keep all classes used by the WebView JavaScript bridge.
# Methods annotated with @JavascriptInterface must remain
# un-obfuscated so that the JS code in index.html can call them.
-keepclassmembers class ru.otvykaniye.tracker.TrackerBridge {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep AppDataStore (used by widgets and the bridge)
-keep class ru.otvykaniye.tracker.AppDataStore { *; }

# Keep widget providers and receivers (referenced in AndroidManifest)
-keep class ru.otvykaniye.tracker.SmallTrackerWidgetProvider
-keep class ru.otvykaniye.tracker.WideTrackerWidgetProvider
-keep class ru.otvykaniye.tracker.WidgetActionReceiver
