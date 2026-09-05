package ru.otvykaniye.tracker

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class WidgetActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_LOG = "ru.otvykaniye.tracker.ACTION_LOG"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (ACTION_LOG != intent.action) return
        if (AppDataStore.recordActiveKind(context)) {
            val manager = AppWidgetManager.getInstance(context)
            update(manager, context, SmallTrackerWidgetProvider::class.java)
            update(manager, context, WideTrackerWidgetProvider::class.java)
        }
    }

    private fun update(manager: AppWidgetManager, context: Context, provider: Class<*>) {
        val name = ComponentName(context, provider)
        val ids = manager.getAppWidgetIds(name)
        if (ids.isNotEmpty()) {
            val refresh = Intent(context, provider).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(refresh)
        }
    }
}
