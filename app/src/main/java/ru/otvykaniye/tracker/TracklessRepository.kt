package ru.otvykaniye.tracker

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TracklessRepository(private val context: Context) {

    val stateFlow: Flow<TracklessState> = context.dataStore.data.map { preferences ->
        val json = preferences[AppDataStore.KEY_STATE] ?: ""
        TracklessState.fromJson(json)
    }

    suspend fun saveState(state: TracklessState) {
        val json = state.toJson().toString()
        AppDataStore.saveStateSuspend(context, json)
    }

    suspend fun recordUse(trigger: String): Boolean {
        return AppDataStore.recordUse(context, trigger)
    }
}
