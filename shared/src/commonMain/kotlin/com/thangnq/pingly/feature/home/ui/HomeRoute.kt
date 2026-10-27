package com.thangnq.pingly.feature.home.ui

import androidx.compose.runtime.Composable
import com.thangnq.pingly.core.mvi.MviScreen
import com.thangnq.pingly.feature.home.presentation.HomeViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeRoute(viewModel: HomeViewModel = koinViewModel()) {
    MviScreen(viewModel) { state, onEvent ->
        HomeScreen(state = state, onEvent = onEvent)
    }
}
