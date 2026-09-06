package ru.otvykaniye.tracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.otvykaniye.tracker.TracklessViewModel
import ru.otvykaniye.tracker.ui.screens.MainScreen
import ru.otvykaniye.tracker.ui.screens.OnboardingScreen

@Composable
fun TracklessApp(viewModel: TracklessViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    if (!state.onboarded) {
        OnboardingScreen(
            state = state,
            onComplete = { newState -> viewModel.completeOnboarding(newState) }
        )
    } else {
        MainScreen(viewModel = viewModel)
    }
}

