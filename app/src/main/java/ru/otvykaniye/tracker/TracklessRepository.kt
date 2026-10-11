package ru.otvykaniye.tracker

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import ru.otvykaniye.tracker.data.repository.ConsumptionRepository
import ru.otvykaniye.tracker.data.repository.SettingsRepository
import ru.otvykaniye.tracker.sync.WidgetSyncManager

class TracklessRepository(
    private val settingsRepository: SettingsRepository,
    private val consumptionRepository: ConsumptionRepository,
    private val widgetSyncManager: WidgetSyncManager
) {

    val stateFlow: Flow<TracklessState> = combine(
        settingsRepository.settingsFlow,
        consumptionRepository.observeAllEntries()
    ) { settings, entriesMap ->
        val mergedProfiles = settings.profiles.mapValues { (kind, profile) ->
            profile.copy(entries = entriesMap[kind] ?: emptyList())
        }
        settings.copy(profiles = mergedProfiles)
    }

    suspend fun getCurrentState(): TracklessState {
        return stateFlow.first()
    }

    suspend fun recordUse(kind: String, trigger: String, customTimestamp: Long? = null): Boolean {
        return try {
            consumptionRepository.recordUse(kind, trigger, customTimestamp)
            widgetSyncManager.syncWidgets()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun undoLastUse(kind: String): Boolean {
        return try {
            val removed = consumptionRepository.undoLastUse(kind)
            if (removed) {
                widgetSyncManager.syncWidgets()
            }
            removed
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteEntry(id: String) {
        consumptionRepository.deleteEntry(id)
        widgetSyncManager.syncWidgets()
    }

    suspend fun saveSettings(state: TracklessState) {
        settingsRepository.saveSettings(state)
        widgetSyncManager.syncWidgets()
    }

    suspend fun resetData() {
        consumptionRepository.clearAll()
        settingsRepository.saveSettings(TracklessState.defaultState())
        widgetSyncManager.syncWidgets()
    }

    suspend fun importState(state: TracklessState) {
        settingsRepository.saveSettings(state)
        consumptionRepository.clearAll()
        val entriesByKind = state.profiles.mapValues { it.value.entries }
        consumptionRepository.importEntries(entriesByKind)
        widgetSyncManager.syncWidgets()
    }
}
