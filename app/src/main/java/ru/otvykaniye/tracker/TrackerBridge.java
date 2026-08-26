package ru.otvykaniye.tracker;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class TrackerBridge {
    private final MainActivity activity;
    private final Context context;
    public TrackerBridge(MainActivity activity) { this.activity = activity; this.context = activity.getApplicationContext(); }
    @JavascriptInterface public String getStateJson() { return AppDataStore.getState(context); }
    @JavascriptInterface public void saveStateJson(String json) { AppDataStore.saveState(context, json); WidgetUi.updateAll(context); }
    @JavascriptInterface public void exportBackup(String json) { activity.exportBackup(json); }
    @JavascriptInterface public void importBackup() { activity.importBackup(); }
    public void showMessage(String message) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show(); }
}
