package ru.otvykaniye.tracker

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.otvykaniye.tracker.di.AppContainer
import ru.otvykaniye.tracker.di.DefaultAppContainer
import ru.otvykaniye.tracker.sync.MidnightWidgetWorker

class TracklessApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Trigger one-time migration from legacy DataStore to Room in background
        CoroutineScope(Dispatchers.IO).launch {
            container.settingsRepository.checkAndMigrateLegacyData(container.database.consumptionDao())
        }

        // Schedule midnight widget refresh
        MidnightWidgetWorker.schedule(this)
    }
}
