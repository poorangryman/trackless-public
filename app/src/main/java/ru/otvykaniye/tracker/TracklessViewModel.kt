package ru.otvykaniye.tracker

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TracklessViewModel(
    private val repository: TracklessRepository
) : ViewModel() {

    val state: StateFlow<TracklessState> = repository.stateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TracklessState.defaultState()
        )

    val timeSinceLastEntry = flow {
        while (true) {
            val currentState = state.value
            val lastEntry = currentState.profiles[currentState.activeKind]?.entries?.lastOrNull()
            val time = if (lastEntry != null) (System.currentTimeMillis() - lastEntry.ts).coerceAtLeast(0L) else 0L
            emit(time)
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setKind(kind: String) {
        viewModelScope.launch {
            val current = state.value
            if (current.activeKind != kind) {
                repository.saveSettings(current.copy(activeKind = kind))
            }
        }
    }

    fun recordUse(trigger: String) {
        recordUseAtTime(trigger, null)
    }

    fun recordUseAtTime(trigger: String, timestamp: Long?) {
        viewModelScope.launch {
            val kind = state.value.activeKind
            repository.recordUse(kind, trigger, timestamp)
        }
    }

    fun undoLastUse() {
        viewModelScope.launch {
            val kind = state.value.activeKind
            repository.undoLastUse(kind)
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteEntry(id)
        }
    }

    fun completeOnboarding(newState: TracklessState) {
        viewModelScope.launch {
            repository.saveSettings(newState.copy(onboarded = true))
        }
    }

    fun saveSettings(newState: TracklessState) {
        viewModelScope.launch {
            repository.saveSettings(newState)
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetData()
        }
    }

    fun importState(newState: TracklessState) {
        viewModelScope.launch {
            repository.importState(newState)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val app = application as TracklessApplication
                    return TracklessViewModel(app.container.tracklessRepository) as T
                }
            }
    }
}
