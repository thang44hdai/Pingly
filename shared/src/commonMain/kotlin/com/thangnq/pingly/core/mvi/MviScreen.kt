package com.thangnq.pingly.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun <State : MviContract.State, Event : MviContract.Event> MviScreen(
    viewModel: MviViewModel<State, Event>,
    content: @Composable (State, (Event) -> Unit) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    content(state, viewModel::onEvent)
}
