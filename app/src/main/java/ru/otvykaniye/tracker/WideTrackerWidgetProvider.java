package ru.otvykaniye.tracker;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
public class WideTrackerWidgetProvider extends AppWidgetProvider {
 @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids){ manager.updateAppWidget(ids,WidgetUi.wide(context)); }
 @Override public void onEnabled(Context context){ WidgetUi.updateAll(context); }
}
