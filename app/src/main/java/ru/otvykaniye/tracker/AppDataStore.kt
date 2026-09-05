package ru.otvykaniye.tracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trackless_datastore",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "trackless_data"))
    }
)

object AppDataStore {
    private val KEY_STATE = stringPreferencesKey("state_json")

    fun getState(context: Context): String {
        return runBlocking {
            context.dataStore.data.map { preferences ->
                preferences[KEY_STATE] ?: ""
            }.first()
        }
    }

    fun saveState(context: Context, json: String?) {
        if (json.isNullOrBlank()) return
        runBlocking {
            context.dataStore.edit { preferences ->
                preferences[KEY_STATE] = json
            }
        }
    }

    fun recordActiveKind(context: Context): Boolean {
        return try {
            val raw = getState(context)
            if (raw.isEmpty()) return false
            val state = JSONObject(raw)
            val kind = state.optString("activeKind", "snus")
            val profiles = state.optJSONObject("profiles") ?: return false
            val profile = profiles.optJSONObject(kind) ?: return false
            var entries = profile.optJSONArray("entries")
            if (entries == null) {
                entries = JSONArray()
                profile.put("entries", entries)
            }
            val entry = JSONObject()
            val now = System.currentTimeMillis()
            entry.put("id", "$now-widget")
            entry.put("ts", now)
            entries.put(entry)
            saveState(context, state.toString())
            true
        } catch (ignored: Exception) {
            false
        }
    }
}
