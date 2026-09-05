package ru.otvykaniye.tracker

import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WideTrackerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
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
                        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis()))
                        for (i in 0 until entries.length()) {
                            val entry = entries.optJSONObject(i) ?: continue
                            val ts = entry.optLong("ts", 0)
                            if (ts <= 0) continue

                            val entryKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ts))
                            if (todayKey == entryKey) today++
                            if (ts > last) last = ts
                        }
                    }
                }
            }
        } catch (ignored: Exception) {}

        provideContent {
            Row(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .background(ImageProvider(R.drawable.widget_bg))
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight().padding(end = 8.dp)
                ) {
                    val kindLabel = if ("snus" == kind) {
                        if ("en" == lang) "SNUS" else "СНЮС"
                    } else {
                        if ("en" == lang) "CIGARETTES" else "СИГАРЕТЫ"
                    }
                    Text(
                        text = "TRACKLESS · $kindLabel",
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(Color(0xFF8FA89B)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    if (last > 0) {
                        val elapsed = Math.max(0L, System.currentTimeMillis() - last)
                        val baseTime = SystemClock.elapsedRealtime() - elapsed
                        AndroidRemoteViews(
                            remoteViews = RemoteViews(context.packageName, R.layout.widget_chrono_wide).apply {
                                setChronometer(R.id.widget_timer, baseTime, "%s", true)
                            }
                        )
                    } else {
                        Text(
                            text = "— : — : —",
                            style = TextStyle(
                                color = androidx.glance.unit.ColorProvider(Color(0xFF8FA89B)),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(3.dp))

                    val countText = if ("en" == lang) "$today today" else "$today сегодня"
                    Text(
                        text = countText,
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(Color(0xFF4ADE80)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Image(
                    provider = ImageProvider(R.drawable.ic_widget_plus),
                    contentDescription = "Add",
                    modifier = GlanceModifier
                        .size(44.dp)
                        .background(ImageProvider(R.drawable.widget_add_bg))
                        .padding(12.dp)
                        .clickable(actionRunCallback<LogActionCallback>())
                )
            }
        }
    }
}
