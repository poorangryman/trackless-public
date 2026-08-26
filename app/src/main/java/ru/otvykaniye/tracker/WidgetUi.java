package ru.otvykaniye.tracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;

public final class WidgetUi {
    private WidgetUi() {}

    public static RemoteViews small(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_small);
        fill(context, views, false);
        return views;
    }

    public static RemoteViews wide(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_wide);
        fill(context, views, true);
        return views;
    }

    private static void fill(Context context, RemoteViews views, boolean wide) {
        int today = 0;
        long last = 0;
        String lang = "ru";

        try {
            String raw = AppDataStore.getState(context);
            if (!raw.isEmpty()) {
                JSONObject state = new JSONObject(raw);
                String kind = state.optString("activeKind", "snus");
                lang = "en".equals(state.optString("language", "ru")) ? "en" : "ru";
                JSONObject profiles = state.optJSONObject("profiles");
                JSONObject profile = profiles == null ? null : profiles.optJSONObject(kind);

                if (profile != null) {
                    JSONArray entries = profile.optJSONArray("entries");
                    if (entries != null) {
                        String todayKey = dayKey(System.currentTimeMillis());
                        for (int i = 0; i < entries.length(); i++) {
                            JSONObject entry = entries.optJSONObject(i);
                            if (entry == null) continue;

                            long ts = entry.optLong("ts", 0);
                            if (ts <= 0) continue;

                            if (todayKey.equals(dayKey(ts))) today++;
                            if (ts > last) last = ts;
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        // The timer is the main information on both widgets.
        // Chronometer keeps counting on the launcher without rebuilding the widget every second.
        views.setViewVisibility(R.id.widget_timer, android.view.View.GONE);
        views.setViewVisibility(R.id.widget_timer_empty, android.view.View.GONE);
        if (last > 0) {
            long elapsed = Math.max(0L, System.currentTimeMillis() - last);
            views.setChronometer(
                    R.id.widget_timer,
                    SystemClock.elapsedRealtime() - elapsed,
                    "%s",
                    true
            );
            views.setViewVisibility(R.id.widget_timer, android.view.View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_timer, android.view.View.GONE);
            views.setTextViewText(R.id.widget_timer_empty, "— : — : —");
            views.setViewVisibility(R.id.widget_timer_empty, android.view.View.VISIBLE);
        }

        // The 2x1 widget can also show today's count; the compact 1x1 stays minimal.
        if (wide) {
            String countText = "en".equals(lang)
                    ? today + (today == 1 ? " today" : " today")
                    : today + " сегодня";
            views.setTextViewText(R.id.widget_count, countText);
            views.setViewVisibility(R.id.widget_count, android.view.View.VISIBLE);
        }

        // Tapping the widget body opens the application.
        Intent openIntent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent openPi = PendingIntent.getActivity(
                context, wide ? 3002 : 3001, openIntent, flags
        );

        // Tapping only "+" records an entry and does not open the application.
        Intent logIntent = new Intent(context, WidgetActionReceiver.class)
                .setAction(WidgetActionReceiver.ACTION_LOG);
        PendingIntent logPi = PendingIntent.getBroadcast(
                context, wide ? 4002 : 4001, logIntent, flags
        );

        views.setOnClickPendingIntent(R.id.widget_root, openPi);
        views.setOnClickPendingIntent(R.id.widget_add, logPi);
    }

    private static String dayKey(long ts) {
        return new java.text.SimpleDateFormat(
                "yyyy-MM-dd",
                java.util.Locale.getDefault()
        ).format(new java.util.Date(ts));
    }

    public static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        ComponentName small = new ComponentName(context, SmallTrackerWidgetProvider.class);
        int[] smallIds = manager.getAppWidgetIds(small);
        if (smallIds.length > 0) {
            manager.updateAppWidget(smallIds, small(context));
        }

        ComponentName wide = new ComponentName(context, WideTrackerWidgetProvider.class);
        int[] wideIds = manager.getAppWidgetIds(wide);
        if (wideIds.length > 0) {
            manager.updateAppWidget(wideIds, wide(context));
        }
    }
}
