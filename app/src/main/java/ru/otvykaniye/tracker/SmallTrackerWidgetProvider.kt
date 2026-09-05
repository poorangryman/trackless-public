package ru.otvykaniye.tracker

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class SmallTrackerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        manager.updateAppWidget(ids, WidgetUi.small(context))
    }
    override fun onEnabled(context: Context) {
        WidgetUi.updateAll(context)
    }
}
