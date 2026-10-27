package com.thangnq.pingly.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<State : MviContract.State, Event : MviContract.Event>(
    initialState: State,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    val state: StateFlow<State> = mutableState.asStateFlow()

    protected fun updateState(transform: (State) -> State) {
        mutableState.update(transform)
    }

    abstract fun onEvent(event: Event)
}
