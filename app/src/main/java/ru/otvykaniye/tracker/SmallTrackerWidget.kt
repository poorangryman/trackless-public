package ru.otvykaniye.tracker

import android.content.Context
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class LogActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        if (AppDataStore.recordUse(context, "widget")) {
            SmallTrackerWidget().updateAll(context)
            WideTrackerWidget().updateAll(context)
        }
    }
}

class SmallTrackerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state by context.dataStore.data
                .collectAsState(initial = androidx.datastore.preferences.core.emptyPreferences())
            val raw = state[AppDataStore.KEY_STATE].orEmpty()

            var last = 0L
            try {
                if (raw.isNotEmpty()) {
                    val parsed = TracklessState.fromJson(raw)
                    val profile = parsed.profiles[parsed.activeKind]
                    last = profile?.entries?.maxOfOrNull { it.ts } ?: 0L
                }
            } catch (ignored: Exception) {}

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(9.dp)
                    .background(ImageProvider(R.drawable.widget_bg))
                    .clickable(actionStartActivity(android.content.Intent(context, MainActivity::class.java))),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "TRACKLESS",
                    style = TextStyle(
                        color = androidx.glance.unit.ColorProvider(Color(0xFF8FA89B)),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = GlanceModifier.height(3.dp))

                if (last > 0) {
                    val elapsed = Math.max(0L, System.currentTimeMillis() - last)
                    val baseTime = SystemClock.elapsedRealtime() - elapsed
                    AndroidRemoteViews(
                        remoteViews = RemoteViews(context.packageName, R.layout.widget_chrono_small).apply {
                            setChronometer(R.id.widget_timer, baseTime, "%s", true)
                        },
                        modifier = GlanceModifier.padding(bottom = 6.dp)
                    )
                } else {
                    Text(
                        text = "— : — : —",
                        style = TextStyle(
                            color = androidx.glance.unit.ColorProvider(Color(0xFF8FA89B)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = GlanceModifier.padding(bottom = 6.dp)
                    )
                }

                Image(
                    provider = ImageProvider(R.drawable.ic_widget_plus),
                    contentDescription = "Add",
                    modifier = GlanceModifier
                        .size(28.dp)
                        .background(ImageProvider(R.drawable.widget_add_bg))
                        .padding(6.dp)
                        .clickable(actionRunCallback<LogActionCallback>())
                )
            }
        }
    }
}
