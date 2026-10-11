package ru.otvykaniye.tracker.di

import android.content.Context
import ru.otvykaniye.tracker.TracklessRepository
import ru.otvykaniye.tracker.data.local.db.AppDatabase
import ru.otvykaniye.tracker.data.repository.ConsumptionRepository
import ru.otvykaniye.tracker.data.repository.ConsumptionRepositoryImpl
import ru.otvykaniye.tracker.data.repository.SettingsRepository
import ru.otvykaniye.tracker.data.repository.SettingsRepositoryImpl
import ru.otvykaniye.tracker.sync.WidgetSyncManager
import ru.otvykaniye.tracker.sync.WidgetSyncManagerImpl

interface AppContainer {
    val database: AppDatabase
    val consumptionRepository: ConsumptionRepository
    val settingsRepository: SettingsRepository
    val widgetSyncManager: WidgetSyncManager
    val tracklessRepository: TracklessRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    override val consumptionRepository: ConsumptionRepository by lazy {
        ConsumptionRepositoryImpl(database.consumptionDao())
    }

    override val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context)
    }

    override val widgetSyncManager: WidgetSyncManager by lazy {
        WidgetSyncManagerImpl(context)
    }

    override val tracklessRepository: TracklessRepository by lazy {
        TracklessRepository(
            settingsRepository = settingsRepository,
            consumptionRepository = consumptionRepository,
            widgetSyncManager = widgetSyncManager
        )
    }
}
