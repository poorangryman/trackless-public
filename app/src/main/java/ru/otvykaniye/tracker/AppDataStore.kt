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
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trackless_datastore",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "trackless_data"))
    }
)

object AppDataStore {
    val KEY_STATE = stringPreferencesKey("state_json")

    suspend fun getState(context: Context): String {
        return context.dataStore.data.map { preferences ->
            preferences[KEY_STATE] ?: ""
        }.first()
    }

    suspend fun saveStateSuspend(context: Context, json: String?) {
        if (json.isNullOrBlank()) return
        context.dataStore.edit { preferences ->
            preferences[KEY_STATE] = json
        }
    }

    suspend fun recordActiveKind(context: Context): Boolean {
        return try {
            var recorded = false
            context.dataStore.edit { preferences ->
                val raw = preferences[KEY_STATE] ?: return@edit
                if (raw.isEmpty()) return@edit

                val state = JSONObject(raw)
                val kind = state.optString("activeKind", "snus")
                val profiles = state.optJSONObject("profiles") ?: return@edit
                val profile = profiles.optJSONObject(kind) ?: return@edit

                var entries = profile.optJSONArray("entries")
                if (entries == null) {
                    entries = JSONArray()
                    profile.put("entries", entries)
                }

                val now = System.currentTimeMillis()
                entries.put(JSONObject().apply {
                    put("id", "$now-widget")
                    put("ts", now)
                })

                preferences[KEY_STATE] = state.toString()
                recorded = true
            }
            recorded
        } catch (ignored: Exception) {
            false
        }
    }
}
