package ru.otvykaniye.tracker.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import ru.otvykaniye.tracker.AppDataStore
import ru.otvykaniye.tracker.ProfileState
import ru.otvykaniye.tracker.TracklessState
import ru.otvykaniye.tracker.data.local.db.ConsumptionDao
import ru.otvykaniye.tracker.data.local.db.ConsumptionEntity
import ru.otvykaniye.tracker.dataStore

interface SettingsRepository {
    val settingsFlow: Flow<TracklessState>
    suspend fun getSettings(): TracklessState
    suspend fun saveSettings(state: TracklessState)
    suspend fun checkAndMigrateLegacyData(dao: ConsumptionDao)
}

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private val KEY_MIGRATED_ROOM = booleanPreferencesKey("migrated_to_room_v1")

    override val settingsFlow: Flow<TracklessState> = context.dataStore.data.map { preferences ->
        val rawJson = preferences[AppDataStore.KEY_STATE] ?: ""
        val state = TracklessState.fromJson(rawJson)
        // Ensure profile entries in memory state do not carry stale data
        state.copy(
            profiles = state.profiles.mapValues { (_, profile) ->
                profile.copy(entries = emptyList())
            }
        )
    }

    override suspend fun getSettings(): TracklessState {
        val rawJson = context.dataStore.data.map { preferences ->
            preferences[AppDataStore.KEY_STATE] ?: ""
        }.first()
        return TracklessState.fromJson(rawJson).copy(
            profiles = TracklessState.fromJson(rawJson).profiles.mapValues { (_, profile) ->
                profile.copy(entries = emptyList())
            }
        )
    }

    override suspend fun saveSettings(state: TracklessState) {
        // Strip entries before saving to DataStore to keep preferences small
        val stateWithoutEntries = state.copy(
            profiles = state.profiles.mapValues { (_, profile) ->
                profile.copy(entries = emptyList())
            }
        )
        context.dataStore.edit { preferences ->
            preferences[AppDataStore.KEY_STATE] = stateWithoutEntries.toJson().toString()
        }
    }

    override suspend fun checkAndMigrateLegacyData(dao: ConsumptionDao) {
        try {
            val preferences = context.dataStore.data.first()
            val alreadyMigrated = preferences[KEY_MIGRATED_ROOM] ?: false
            if (alreadyMigrated) return

            val rawJson = preferences[AppDataStore.KEY_STATE] ?: ""
            if (rawJson.isNotBlank()) {
                val fullState = TracklessState.fromJson(rawJson)
                val entitiesToInsert = mutableListOf<ConsumptionEntity>()

                fullState.profiles.forEach { (kind, profile) ->
                    profile.entries.forEach { entry ->
                        entitiesToInsert.add(ConsumptionEntity.fromModel(entry, kind))
                    }
                }

                if (entitiesToInsert.isNotEmpty()) {
                    dao.insertAll(entitiesToInsert)
                }

                // Clean up entries from DataStore and mark migrated
                val strippedState = fullState.copy(
                    profiles = fullState.profiles.mapValues { (_, p) ->
                        p.copy(entries = emptyList())
                    }
                )

                context.dataStore.edit { prefs ->
                    prefs[AppDataStore.KEY_STATE] = strippedState.toJson().toString()
                    prefs[KEY_MIGRATED_ROOM] = true
                }
            } else {
                context.dataStore.edit { prefs ->
                    prefs[KEY_MIGRATED_ROOM] = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
