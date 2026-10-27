package com.thangnq.pingly.feature.home.presentation

import com.thangnq.pingly.core.mvi.MviContract

data class HomePingItemUi(
    val id: Long,
    val message: String,
)

sealed interface HomeInputError {
    data object Empty : HomeInputError
    data class TooLong(val maxLength: Int) : HomeInputError
}

data class HomeUiState(
    val draft: String = "",
    val items: List<HomePingItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val loadError: Boolean = false,
    val saveError: Boolean = false,
    val inputError: HomeInputError? = null,
) : MviContract.State

sealed interface HomeEvent : MviContract.Event {
    data class DraftChanged(val value: String) : HomeEvent
    data object Submit : HomeEvent
    data object RetryLoad : HomeEvent
}
