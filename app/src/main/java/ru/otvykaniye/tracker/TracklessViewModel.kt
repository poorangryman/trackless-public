package ru.otvykaniye.tracker

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TracklessViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TracklessRepository(application)
    private val stateMutex = Mutex()

    private val _state = MutableStateFlow(TracklessState.defaultState())
    val state: StateFlow<TracklessState> = _state.asStateFlow()

    val timeSinceLastEntry = kotlinx.coroutines.flow.flow {
        while (true) {
            val currentState = _state.value
            val lastEntry = currentState.profiles[currentState.activeKind]?.entries?.lastOrNull()
            val time = if (lastEntry != null) System.currentTimeMillis() - lastEntry.ts else 0L
            emit(time)
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0L)

    init {
        viewModelScope.launch {
            repository.stateFlow.collect { persistedState ->
                stateMutex.withLock {
                    _state.value = persistedState
                }
            }
        }
    }

    fun setKind(kind: String) {
        updateState { currentState ->
            if (currentState.activeKind == kind) currentState
            else currentState.copy(activeKind = kind)
        }
    }

    fun recordUse(trigger: String) {
        recordUseAtTime(trigger, null)
    }

    fun recordUseAtTime(trigger: String, timestamp: Long?) {
        viewModelScope.launch {
            val recorded = stateMutex.withLock {
                repository.recordUse(trigger, timestamp)
            }
            if (recorded) {
                try {
                    SmallTrackerWidget().updateAll(getApplication())
                    WideTrackerWidget().updateAll(getApplication())
                } catch (ignored: Exception) {}
            }
        }
    }

    fun undoLastUse() {
        updateState { currentState ->
            val kind = currentState.activeKind
            val profile = currentState.profiles[kind] ?: return@updateState currentState
            if (profile.entries.isEmpty()) return@updateState currentState
            val newProfile = profile.copy(entries = profile.entries.dropLast(1))
            val newProfiles = currentState.profiles.toMutableMap()
            newProfiles[kind] = newProfile
            currentState.copy(profiles = newProfiles)
        }
    }

    fun deleteEntry(id: String) {
        updateState { currentState ->
            val kind = currentState.activeKind
            val profile = currentState.profiles[kind] ?: return@updateState currentState
            val newProfile = profile.copy(entries = profile.entries.filter { it.id != id })
            val newProfiles = currentState.profiles.toMutableMap()
            newProfiles[kind] = newProfile
            currentState.copy(profiles = newProfiles)
        }
    }

    fun completeOnboarding(newState: TracklessState) {
        updateState { newState.copy(onboarded = true) }
    }

    fun saveSettings(newState: TracklessState) {
        updateState { newState }
    }

    fun resetData() {
        updateState { TracklessState.defaultState() }
    }

    fun importState(newState: TracklessState) {
        updateState { newState }
    }

    private fun updateState(transform: (TracklessState) -> TracklessState) {
        viewModelScope.launch {
            stateMutex.withLock {
                val newState = transform(_state.value)
                _state.value = newState
                repository.saveState(newState)
            }

            try {
                SmallTrackerWidget().updateAll(getApplication())
                WideTrackerWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
