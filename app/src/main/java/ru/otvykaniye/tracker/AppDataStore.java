package ru.otvykaniye.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public final class AppDataStore {
    private static final String PREFS = "trackless_data";
    private static final String KEY_STATE = "state_json";

    private AppDataStore() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static String getState(Context context) {
        return prefs(context).getString(KEY_STATE, "");
    }

    public static void saveState(Context context, String json) {
        if (json == null || json.trim().isEmpty()) return;
        prefs(context).edit().putString(KEY_STATE, json).commit();
    }

    public static boolean recordActiveKind(Context context) {
        try {
            String raw = getState(context);
            if (raw.isEmpty()) return false;
            JSONObject state = new JSONObject(raw);
            String kind = state.optString("activeKind", "snus");
            JSONObject profiles = state.optJSONObject("profiles");
            if (profiles == null) return false;
            JSONObject profile = profiles.optJSONObject(kind);
            if (profile == null) return false;
            JSONArray entries = profile.optJSONArray("entries");
            if (entries == null) {
                entries = new JSONArray();
                profile.put("entries", entries);
            }
            JSONObject entry = new JSONObject();
            long now = System.currentTimeMillis();
            entry.put("id", now + "-widget");
            entry.put("ts", now);
            entries.put(entry);
            saveState(context, state.toString());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
