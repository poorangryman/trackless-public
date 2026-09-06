package ru.otvykaniye.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking
import java.util.UUID

class TracklessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TracklessRepository(application)

    val state: StateFlow<TracklessState> = repository.stateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = repository.getStateBlocking()
        )

    private val _timeSinceLastEntry = MutableStateFlow(0L)
    val timeSinceLastEntry = _timeSinceLastEntry.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                updateTimeSinceLastEntry()
                delay(1000)
            }
        }
    }

    private fun updateTimeSinceLastEntry() {
        val currentState = state.value
        val profile = currentState.profiles[currentState.activeKind]
        val lastEntry = profile?.entries?.lastOrNull()
        if (lastEntry != null) {
            _timeSinceLastEntry.value = System.currentTimeMillis() - lastEntry.ts
        } else {
            _timeSinceLastEntry.value = 0L
        }
    }

    fun setKind(kind: String) {
        val currentState = state.value
        if (currentState.activeKind == kind) return
        val newState = currentState.copy(activeKind = kind)
        save(newState)
    }

    fun recordUse(trigger: String) {
        val currentState = state.value
        val kind = currentState.activeKind
        val profile = currentState.profiles[kind] ?: return
        val newEntry = ConsumptionEntry(
            id = "${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(5)}",
            ts = System.currentTimeMillis(),
            trigger = trigger
        )
        val newProfile = profile.copy(entries = profile.entries + newEntry)
        val newProfiles = currentState.profiles.toMutableMap()
        newProfiles[kind] = newProfile
        save(currentState.copy(profiles = newProfiles))
        updateTimeSinceLastEntry()
    }

    fun undoLastUse() {
        val currentState = state.value
        val kind = currentState.activeKind
        val profile = currentState.profiles[kind] ?: return
        if (profile.entries.isEmpty()) return
        val newProfile = profile.copy(entries = profile.entries.dropLast(1))
        val newProfiles = currentState.profiles.toMutableMap()
        newProfiles[kind] = newProfile
        save(currentState.copy(profiles = newProfiles))
        updateTimeSinceLastEntry()
    }

    fun deleteEntry(id: String) {
        val currentState = state.value
        val kind = currentState.activeKind
        val profile = currentState.profiles[kind] ?: return
        val newProfile = profile.copy(entries = profile.entries.filter { it.id != id })
        val newProfiles = currentState.profiles.toMutableMap()
        newProfiles[kind] = newProfile
        save(currentState.copy(profiles = newProfiles))
        updateTimeSinceLastEntry()
    }

    fun completeOnboarding(newState: TracklessState) {
        save(newState.copy(onboarded = true))
    }

    fun saveSettings(newState: TracklessState) {
        save(newState)
    }

    fun resetData() {
        save(TracklessState.defaultState())
    }

    fun importState(newState: TracklessState) {
        save(newState)
    }

    private fun save(newState: TracklessState) {
        viewModelScope.launch {
            repository.saveState(newState)
            // Update Glance Widgets
            try {
                SmallTrackerWidget().updateAll(getApplication())
                WideTrackerWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

