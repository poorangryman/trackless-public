package ru.otvykaniye.tracker.sync

import android.content.Context
import androidx.glance.appwidget.updateAll
import ru.otvykaniye.tracker.SmallTrackerWidget
import ru.otvykaniye.tracker.WideTrackerWidget

interface WidgetSyncManager {
    suspend fun syncWidgets()
}

class WidgetSyncManagerImpl(
    private val context: Context
) : WidgetSyncManager {
    override suspend fun syncWidgets() {
        try {
            SmallTrackerWidget().updateAll(context)
            WideTrackerWidget().updateAll(context)
        } catch (ignored: Exception) {}
    }
}
