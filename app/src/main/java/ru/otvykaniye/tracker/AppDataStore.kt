package ru.otvykaniye.tracker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "trackless_datastore",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "trackless_data"))
    }
)

object AppDataStore {
    val KEY_STATE = stringPreferencesKey("state_json")
}
