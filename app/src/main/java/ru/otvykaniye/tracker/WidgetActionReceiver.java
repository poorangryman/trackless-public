package ru.otvykaniye.tracker;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class WidgetActionReceiver extends BroadcastReceiver {
    public static final String ACTION_LOG = "ru.otvykaniye.tracker.ACTION_LOG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION_LOG.equals(intent.getAction())) return;
        if (AppDataStore.recordActiveKind(context)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            update(manager, context, SmallTrackerWidgetProvider.class);
            update(manager, context, WideTrackerWidgetProvider.class);
        }
    }

    private void update(AppWidgetManager manager, Context context, Class<?> provider) {
        ComponentName name = new ComponentName(context, provider);
        int[] ids = manager.getAppWidgetIds(name);
        if (ids.length > 0) {
            Intent refresh = new Intent(context, provider);
            refresh.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            context.sendBroadcast(refresh);
        }
    }
}
