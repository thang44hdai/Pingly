package com.thangnq.pingly.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.CreationExtras
import com.thangnq.pingly.feature.ping.domain.usecase.CreatePing
import com.thangnq.pingly.feature.ping.domain.usecase.ObservePingFeed
import com.thangnq.pingly.feature.home.presentation.HomeEvent
import com.thangnq.pingly.feature.home.presentation.HomeInputError
import com.thangnq.pingly.feature.home.presentation.HomeViewModel
import com.thangnq.pingly.feature.ping.FakePingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @Test
    fun submitUpdatesStateAndFeed() = runTest {
        val repository = FakePingRepository()
        val store = ViewModelStore()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = createViewModel(store, repository)
            runCurrent()
            assertFalse(viewModel.state.value.isLoading)

            viewModel.onEvent(HomeEvent.DraftChanged("  Remember this  "))
            viewModel.onEvent(HomeEvent.Submit)
            runCurrent()

            assertEquals("", viewModel.state.value.draft)
            assertEquals(listOf("Remember this"), repository.addedMessages)
            assertEquals("Remember this", viewModel.state.value.items.single().message)
            assertFalse(viewModel.state.value.isSubmitting)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun invalidMessageBecomesInputStateWithoutWriting() = runTest {
        val repository = FakePingRepository()
        val store = ViewModelStore()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = createViewModel(store, repository)
            runCurrent()

            viewModel.onEvent(HomeEvent.DraftChanged("  "))
            viewModel.onEvent(HomeEvent.Submit)
            runCurrent()

            assertEquals(HomeInputError.Empty, viewModel.state.value.inputError)
            assertEquals(emptyList(), repository.addedMessages)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    private fun createViewModel(
        store: ViewModelStore,
        repository: FakePingRepository,
    ): HomeViewModel {
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
                HomeViewModel(CreatePing(repository), ObservePingFeed(repository)) as T
        }
        return ViewModelProvider.create(store, factory)[HomeViewModel::class]
    }
}
