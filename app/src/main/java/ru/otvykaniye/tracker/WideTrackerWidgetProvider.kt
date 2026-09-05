package ru.otvykaniye.tracker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class WideTrackerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, WidgetUi.wide(context))
    }
    override fun onEnabled(context: Context) {
        WidgetUi.updateAll(context)
    }
}
