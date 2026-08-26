package ru.otvykaniye.tracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import java.nio.charset.StandardCharsets;

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
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TEXT, json);
        i.putExtra(Intent.EXTRA_TITLE, "trackless-backup.json");
        activity.startActivity(Intent.createChooser(i, "Экспорт данных"));
    }

    @JavascriptInterface
    public void importState() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/json");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(i, MainActivity.IMPORT_REQUEST);
    }
}
