# Shared MVI contract

## Ownership and dependency direction

Keep presentation → domain contracts ← data implementations. Features depend on
shared contracts rather than another feature's implementation. Extract use cases
for reusable business rules or orchestration; avoid one-line forwarding layers.
Use immutable domain/UI values and copy collections instead of mutating backing lists.
Do not add `@Immutable` unless every exposed property satisfies that promise.

The following is a commonMain example, requiring compatible multiplatform
Lifecycle ViewModel and kotlinx-coroutines dependencies. UI resolves error codes
to localized resources; no Android resource IDs cross the boundary.

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Item(val id: String, val title: String, val isArchived: Boolean)
data class ItemUi(val id: String, val title: String)
enum class LoadError { Unavailable }
data class FeatureState(
    val items: List<ItemUi> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: LoadError? = null,
)
sealed interface FeatureEvent {
    data object Refresh : FeatureEvent
    data class QueryChanged(val query: String) : FeatureEvent
    data class ItemClicked(val id: String) : FeatureEvent
}
sealed interface FeatureEffect {
    data class OpenDetail(val id: String) : FeatureEffect
}
interface FeatureRepository {
    // Implementations return snapshot lists and propagate cancellation.
    suspend fun load(): List<Item>
}

// A meaningful domain operation; avoid a UseCase that only forwards one call.
class GetVisibleItems(private val repository: FeatureRepository) {
    suspend operator fun invoke(): List<Item> =
        repository.load().filterNot { it.isArchived }
}

class FeatureViewModel(
    private val getVisibleItems: GetVisibleItems,
) : ViewModel() {
    private val _state = MutableStateFlow(FeatureState())
    val state: StateFlow<FeatureState> = _state.asStateFlow()
    private val _effects = Channel<FeatureEffect>(Channel.BUFFERED)
    val effects: Flow<FeatureEffect> = _effects.receiveAsFlow()
    private var refreshJob: Job? = null

    // UI dispatches on Main. Serialize callers if events originate elsewhere.
    fun onEvent(event: FeatureEvent) {
        when (event) {
            FeatureEvent.Refresh -> refresh()
            is FeatureEvent.QueryChanged ->
                _state.update { it.copy(query = event.query) }
            is FeatureEvent.ItemClicked -> viewModelScope.launch {
                _effects.send(FeatureEffect.OpenDetail(event.id))
            }
        }
    }

    // Ignore duplicate refreshes while one is active.
    private fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val items = getVisibleItems().map { ItemUi(it.id, it.title) }
                _state.update { it.copy(items = items, isLoading = false) }
            } catch (cancelled: CancellationException) {
                _state.update { it.copy(isLoading = false) }
                throw cancelled
            } catch (failure: Exception) {
                // Map known domain failures more precisely in production.
                _state.update {
                    it.copy(isLoading = false, error = LoadError.Unavailable)
                }
            }
        }
    }
}
```

## Concurrency and delivery semantics

Choose an explicit policy per operation: ignore duplicate refreshes as above,
serialize writes, or cancel previous searches and reject stale results using a
request identity. Atomic update alone does not prevent old network results from
replacing new ones. Never perform IO or emit effects inside an update lambda.
Avoid swallowing cancellation through `runCatching` or repository Result wrappers.
Inject dispatchers when work needs offloading; do not assume JVM IO APIs exist in
common code. No GlobalScope or unmanaged per-screen scopes.

The example Channel is a bounded, single-consumer transient queue. A send can
suspend while no UI collects and the buffer fills. Events can be lost between
receipt and UI handling; multiple collectors compete. It is not exactly-once
navigation, persistence, or a broadcast mechanism. Keep payment/save/auth outcomes
in state or persistent storage with acknowledgement when delivery matters. Define
whether a delayed navigation should be discarded after a screen stops.

Use `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)` for derived
repository flows when subscriber-driven collection is intended. Explicitly owned
init collectors are valid when needed; do not mechanically wrap all collection in
stateIn. Do not store two independently mutable copies of the same data.

## Behavioral tests

Place portable tests in commonTest with kotlinx-coroutines-test and the project's
existing assertion/Flow test tools. Control Main for ViewModel tests, use fakes and
virtual time, and clear the ViewModelStore to verify cancellation. Test:

- Initial state, successful load, failure, and retry with existing data retained.
- Query changes during load survive completion; duplicate refresh follows policy.
- Cancellation propagates and never becomes a user-visible failure.
- Rapid operations cannot publish stale results or duplicate writes.
- One effect consumer, screen disposal, and background/foreground delivery policy.

Run those tests on Android and an iOS simulator. JVM-only execution does not catch
Kotlin/Native dependencies or platform implementations. Host/UI smoke tests cover
navigation ownership, localized errors, safe-area insets, and lifecycle cleanup.
