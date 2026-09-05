package ru.otvykaniye.tracker

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WidgetUi {

    fun small(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_small)
        fill(context, views, false)
        return views
    }

    fun wide(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_wide)
        fill(context, views, true)
        return views
    }

    private fun fill(context: Context, views: RemoteViews, wide: Boolean) {
        var today = 0
        var last: Long = 0
        var lang = "ru"
        var kind = "snus"

        try {
            val raw = AppDataStore.getState(context)
            if (raw.isNotEmpty()) {
                val state = JSONObject(raw)
                kind = state.optString("activeKind", "snus")
                lang = if ("en" == state.optString("language", "ru")) "en" else "ru"
                val profiles = state.optJSONObject("profiles")
                val profile = profiles?.optJSONObject(kind)

                if (profile != null) {
                    val entries = profile.optJSONArray("entries")
                    if (entries != null) {
                        val todayKey = dayKey(System.currentTimeMillis())
                        for (i in 0 until entries.length()) {
                            val entry = entries.optJSONObject(i) ?: continue
                            val ts = entry.optLong("ts", 0)
                            if (ts <= 0) continue

                            if (todayKey == dayKey(ts)) today++
                            if (ts > last) last = ts
                        }
                    }
                }
            }
        } catch (ignored: Exception) {}

        views.setViewVisibility(R.id.widget_timer, View.GONE)
        views.setViewVisibility(R.id.widget_timer_empty, View.GONE)
        if (last > 0) {
            val elapsed = Math.max(0L, System.currentTimeMillis() - last)
            views.setChronometer(
                R.id.widget_timer,
                SystemClock.elapsedRealtime() - elapsed,
                "%s",
                true
            )
            views.setViewVisibility(R.id.widget_timer, View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_timer, View.GONE)
            views.setTextViewText(R.id.widget_timer_empty, "ó : ó : ó")
            views.setViewVisibility(R.id.widget_timer_empty, View.VISIBLE)
        }

        if (wide) {
            val countText = if ("en" == lang) "$today today" else "$today ÒÂ„Ó‰Ìˇ"
            views.setTextViewText(R.id.widget_count, countText)
            views.setViewVisibility(R.id.widget_count, View.VISIBLE)

            val kindLabel = if ("snus" == kind) {
                if ("en" == lang) "SNUS" else "—Õﬁ—"
            } else {
                if ("en" == lang) "CIGARETTES" else "—»√¿–≈“€"
            }
            views.setTextViewText(R.id.widget_title, "TRACKLESS ∑ $kindLabel")
        } else {
            views.setTextViewText(R.id.widget_title, "TRACKLESS")
        }

        val openIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val openPi = PendingIntent.getActivity(
            context, if (wide) 3002 else 3001, openIntent, flags
        )

        val logIntent = Intent(context, WidgetActionReceiver::class.java)
            .setAction(WidgetActionReceiver.ACTION_LOG)
        val logPi = PendingIntent.getBroadcast(
            context, if (wide) 4002 else 4001, logIntent, flags
        )

        views.setOnClickPendingIntent(R.id.widget_root, openPi)
        views.setOnClickPendingIntent(R.id.widget_add, logPi)
    }

    private fun dayKey(ts: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ts))
    }

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)

        val small = ComponentName(context, SmallTrackerWidgetProvider::class.java)
        val smallIds = manager.getAppWidgetIds(small)
        if (smallIds.isNotEmpty()) {
            manager.updateAppWidget(smallIds, small(context))
        }

        val wide = ComponentName(context, WideTrackerWidgetProvider::class.java)
        val wideIds = manager.getAppWidgetIds(wide)
        if (wideIds.isNotEmpty()) {
            manager.updateAppWidget(wideIds, wide(context))
        }
    }
}
