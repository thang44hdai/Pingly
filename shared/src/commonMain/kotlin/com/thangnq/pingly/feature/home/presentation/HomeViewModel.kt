package com.thangnq.pingly.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.thangnq.pingly.core.mvi.MviViewModel
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePing
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePingResult
import com.thangnq.pingly.feature.ping.domain.usecase.ObservePingFeed
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeViewModel(
    private val createPing: CreatePing,
    private val observePingFeed: ObservePingFeed,
) : MviViewModel<HomeUiState, HomeEvent>(HomeUiState()) {

    private var feedJob: Job? = null
    private var submitJob: Job? = null

    init {
        observeFeed()
    }

    /** UI events are delivered on the main dispatcher by the Compose host. */
    override fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.DraftChanged -> updateState {
                it.copy(draft = event.value, inputError = null, saveError = false)
            }
            HomeEvent.Submit -> submit()
            HomeEvent.RetryLoad -> observeFeed()
        }
    }

    private fun observeFeed() {
        feedJob?.cancel()
        feedJob = viewModelScope.launch {
            updateState { it.copy(isLoading = true, loadError = false) }
            try {
                observePingFeed().collect { pings ->
                    val items = pings.map { HomePingItemUi(id = it.id, message = it.message) }
                    updateState { current ->
                        current.copy(items = items, isLoading = false, loadError = false)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(isLoading = false, loadError = true) }
            }
        }
    }

    private fun submit() {
        if (submitJob?.isActive == true) return
        val submittedDraft = state.value.draft
        submitJob = viewModelScope.launch {
            updateState {
                it.copy(isSubmitting = true, inputError = null, saveError = false)
            }
            try {
                when (val result = createPing(submittedDraft)) {
                    is CreatePingResult.Created -> updateState { current ->
                        current.copy(
                            draft = if (current.draft == submittedDraft) "" else current.draft,
                            isSubmitting = false,
                        )
                    }
                    is CreatePingResult.Invalid -> updateState {
                        it.copy(
                            isSubmitting = false,
                            inputError = when (val reason = result.reason) {
                                CreatePingResult.InvalidReason.Empty -> HomeInputError.Empty
                                is CreatePingResult.InvalidReason.TooLong ->
                                    HomeInputError.TooLong(reason.maxLength)
                            },
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(isSubmitting = false, saveError = true) }
            }
        }
    }
}
