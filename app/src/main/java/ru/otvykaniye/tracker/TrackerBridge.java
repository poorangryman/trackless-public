package ru.otvykaniye.tracker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class TrackerBridge {
    private final MainActivity activity;
    public TrackerBridge(MainActivity activity) { this.activity = activity; }

    @JavascriptInterface
    public String getStateJson() { return AppDataStore.getState(activity); }

    @JavascriptInterface
    public void saveStateJson(String json) {
        AppDataStore.saveState(activity, json);
        WidgetUi.updateAll(activity);
    }

    @JavascriptInterface
    public void exportState(String json) {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("application/json");
            i.putExtra(Intent.EXTRA_TEXT, json);
            i.putExtra(Intent.EXTRA_TITLE, "trackless-backup.json");
            activity.startActivity(Intent.createChooser(i, null));
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(activity, "No sharing app available", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("deprecation")
    @JavascriptInterface
    public void importState() {
        try {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("application/json");
            i.addCategory(Intent.CATEGORY_OPENABLE);
            activity.startActivityForResult(i, MainActivity.IMPORT_REQUEST);
        } catch (ActivityNotFoundException ignored) {
            Toast.makeText(activity, "No file manager available", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void haptic(String type) {
        activity.runOnUiThread(() -> {
            try {
                android.os.Vibrator v = (android.os.Vibrator) activity.getSystemService(android.content.Context.VIBRATOR_SERVICE);
                if (v == null || !v.hasVibrator()) return;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    if ("tick".equalsIgnoreCase(type) || "light".equalsIgnoreCase(type)) {
                        v.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_TICK));
                    } else if ("heavy".equalsIgnoreCase(type) || "success".equalsIgnoreCase(type)) {
                        v.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_HEAVY_CLICK));
                    } else {
                        v.vibrate(android.os.VibrationEffect.createPredefined(android.os.VibrationEffect.EFFECT_CLICK));
                    }
                } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    int duration = "tick".equalsIgnoreCase(type) ? 12 : 35;
                    v.vibrate(android.os.VibrationEffect.createOneShot(duration, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    v.vibrate("tick".equalsIgnoreCase(type) ? 12 : 35);
                }
            } catch (Exception ignored) {}
        });
    }
}
