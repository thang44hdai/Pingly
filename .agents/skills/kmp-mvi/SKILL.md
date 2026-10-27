---
name: kmp-mvi
description: Build and review shared MVI features for Kotlin Multiplatform Android and iOS apps, including StateFlow state, effects, lifecycle, concurrency, Clean Architecture use cases, and host integration.
---

# KMP MVI — Android and iOS

Use unidirectional flow, Clean Architecture boundaries, repository contracts,
and atomic state updates in shared Kotlin code. For module and dependency
boundaries, use [kmp-architecture](../kmp-architecture/SKILL.md).

## Start with the actual project

Inspect the existing Gradle configuration, version catalog, plugins, source sets,

Android app, iOS host, framework integration, and existing architecture before

changing code. Preserve working toolchain, UI, DI, navigation, and state-management

choices; do not migrate them just to use MVI.

For a new project, default to shared Compose Multiplatform UI with small native

hosts, unless the user requests SwiftUI or platform-native UI.

Keep Android-only Hilt, `Context`, `R.string`, and platform Gradle APIs out of
shared code. Verify compatibility before introducing or changing dependencies.
Do not invent version pins or upgrade dependencies without a concrete requirement.

Load these references directly as needed:

| Reference | Load when |
|---|---|
| [Shared MVI contract](references/shared-mvi.md) | Implementing state, events, concurrency, effects, or state transitions |
| [Platform integration](references/platform-integration.md) | Wiring source sets, DI, resources, shared UI, or SwiftUI |
| [Build verification](references/build-verification.md) | Build-related work or a request to prove Android/iOS execution; these are not mandatory checks for a documentation review |

## Architectural contract

Follow MVI with Clean Architecture:

```text

Android UI / Compose UI / SwiftUI

                |

                v

              Event

                |

                v

       Shared ViewModel

                |

                +----> UseCase

                |          |

                |          v

                |      Repository

                |          |

                |          v

                |     Data Sources

                |          |

                |          v

                |     API / Database

                |          |

                |          v

                |      Repository

                |          |

                <----------+

                |

                v

       StateFlow<UiState>

                |

                v

             Render UI

       Transient Effect

                |

                v

      Appropriate UI consumer

```

The data result flows back through Repository and UseCase to ViewModel, which

publishes the updated state.

### Layer responsibilities

- **UI:** renders state and sends user events. It does not implement business rules.

- **ViewModel:** receives events, coordinates use cases, manages screen state,

  handles presentation-level errors, and emits appropriate effects.

- **UseCase:** encapsulates a business operation or rule and coordinates domain

  behavior. It should not depend on ViewModel or UI frameworks.

- **Repository interface:** defines the domain-facing contract for accessing

  or modifying data.

- **Repository implementation:** belongs to the data layer and coordinates

  remote APIs, local databases, caches, or other data sources.

Keep domain models, repository interfaces, use cases, and portable MVI logic

in `commonMain` when they are platform-independent. Place repository

implementations and platform-specific data sources in the appropriate source

sets or modules according to the existing project architecture.

Do not make ViewModel call a data source or repository implementation directly

when the project requires UseCase-based Clean Architecture.

Do not create a UseCase for every trivial function mechanically. Create use cases

around meaningful business operations and follow the existing project conventions.

If a use case is already responsible for an operation, reuse it rather than

duplicating the same business logic in ViewModel.

## State, Event, Effect, and reducer rules

### Event

An Event represents an action from the UI or another explicitly defined input

source, such as `Load`, `Retry`, `SelectDate`, `Search`, or `Submit`.

- Use consistent naming across the project.

- Events describe inputs, not resulting UI state.

- Do not introduce a generic event-dispatch framework when direct functions

  are clearer for a simple feature.

- For complex features, use a sealed interface or another type-safe contract

  that makes supported actions explicit.

### State

State represents the current, renderable condition of a screen.

- Model state as immutable data.

- Expose `StateFlow<UiState>` and keep mutable state private.

- Update state using `_state.update { current -> current.copy(...) }` or an

  equivalent safe mechanism consistent with the project.

- Do not mutate collections or objects already published through state.

- Prefer immutable collections where already supported; otherwise ensure

  published collections are not mutated after emission.

- Keep state sufficient to render the UI when observation starts or the screen

  is recreated.

- Model loading, success, empty, and error states explicitly where needed.

- Avoid contradictory state combinations. Use sealed state types when states

  are mutually exclusive and a single model would permit invalid combinations.

### State decomposition

Do not force every screen property into one large, flat data class.

- Group related state by responsibility using nested data classes, such as

  `ListState`, `FilterState`, `FormState`, or `DialogState`.

- Keep tightly related values together when they must remain consistent.

- Split state holders or ViewModels only when there is a meaningful ownership,

  lifecycle, or feature boundary.

- Do not create multiple `StateFlow` instances solely to avoid copying a state

  object. Separate flows only when independent ownership or update requirements

  justify the additional complexity.

- Do not introduce nested classes for every property; keep simple features simple.

Example:

```kotlin

data class ScreenUiState(

    val list: ListState = ListState(),

    val filter: FilterState = FilterState(),

    val dialog: DialogState = DialogState()

)

data class ListState(

    val items: List<Item> = emptyList(),

    val isLoading: Boolean = false,

    val error: UiError? = null

)

data class FilterState(

    val query: String = "",

    val selectedCategory: String? = null

)

data class DialogState(

    val showConfirmation: Boolean = false

)

```

Update only the affected state branch:

```kotlin

_state.update { current ->

    current.copy(

        filter = current.filter.copy(query = newQuery)

    )

}

```

`data class.copy()` performs a shallow copy. Unchanged child objects and

collections can retain their references. Do not introduce deep copying or

rebuild large collections without a functional requirement.

### Independent API sections and partial rendering

For screens composed of multiple sections backed by independent APIs, prefer one

screen-level data class state with a separate state branch for each section.

Each section may have a different response or UI model type, and each section

should be able to render as soon as its own request succeeds without waiting for

the other requests.

Use a reusable generic sealed type when each section has mutually exclusive

loading, success, and error states:

```kotlin

sealed interface SectionState<out T> {
data object Loading : SectionState<Nothing>

data class Success<T>(val data: T) : SectionState<T>

data class Error(val message: UiError) : SectionState<Nothing>
}

data class ScreenUiState(
val user: SectionState<UserUiModel> = SectionState.Loading,

val products: SectionState<List<ProductUiModel>> = SectionState.Loading,

val promotions: SectionState<PromotionUiModel> = SectionState.Loading
)

```

Adapt error and model types to the existing project. Prefer UI/domain models over

exposing transport DTOs directly to the UI when that matches the architecture.

- Keep the overall screen state as a data class when sections can coexist and

  update independently.

- Use sealed interface or sealed class for each section's mutually exclusive

  state variants, such as Loading, Success, and Error. These types complement

  a data class; they are not competing alternatives.

- Start independent UseCases concurrently when the APIs are independent and

  one result is not required before requesting another.

- Publish each result as soon as it is available by updating only that section

  with _state.update { current -> current.copy(section = newSectionState) }.

  Do not wait for all requests to finish before publishing successful results.

- Keep a request failure local to its section when other sections can still be

  displayed. Use a screen-level error only when the whole screen truly cannot proceed.

- Preserve coroutine cancellation. Do not use runCatching without rethrowing

  CancellationException; cancellation must not become an ordinary section error.

- Define a stale-response policy if a section can be refreshed repeatedly or

  requests can overlap. Prevent older results replacing newer results when

  latest-request semantics are required.

- Keep each section's UI rendering focused on its own state. A single

  StateFlow<ScreenUiState> is usually sufficient; do not create separate flows

  solely to avoid copying screen state. Split observation only for a meaningful

  ownership, lifecycle, or performance reason.

- Independent section state does not guarantee that every UI framework updates

  only that section. Observation and recomposition depend on the framework and

  how state is collected and passed to child views.

Example of updating only one section:

```kotlin

_state.update { current ->
current.copy(

    user = SectionState.Success(userUiModel)

)
}

```

For concurrent loading, follow the project's coroutine and error-handling

conventions. Ensure each request can publish its own result independently, and

do not accidentally swallow cancellation when mapping failures to UI state.

### Atomic updates and reducers

- Use `_state.update { current -> ... }` for safe atomic state transformations.

- Update lambdas may execute more than once under contention.

- Keep update lambdas and reducers pure. Do not perform IO, suspend calls,

  navigation, or external side effects inside them.

- Perform asynchronous work outside the update lambda, then publish results

  through a safe state transition.

- Introduce a separate reducer when it improves separation, determinism, reuse,

  or clarity. A reducer is not mandatory for every feature.

- Do not assume an atomic state update makes an entire multi-step business

  operation atomic.

### Effect

Effect represents a transient signal requiring handling by an appropriate UI

consumer, such as navigation, opening a platform share sheet, or showing

a snackbar.

- Do not use Effect as the source of truth for loading, validation errors,

  transaction status, or other state that must remain observable.

- Do not assume `Channel`, `SharedFlow`, or lifecycle-aware collection guarantees

  exactly-once delivery across cancellation, process termination, or host recreation.

- Define buffering, replay, consumer count, and delivery behavior according

  to the actual use case.

- Avoid replaying navigation or notifications unintentionally after UI recreation.

- Keep critical outcomes, such as payment completion, authentication status,

  and persisted changes, in authoritative state or durable storage.

- Do not treat an emitted UI Effect as proof that a business operation succeeded.

Do not add an Effect abstraction when a direct, platform-appropriate callback

is simpler and consistent with the existing architecture.

## Coroutine, lifecycle, and concurrency rules

- Give every coroutine an explicit owner and appropriate scope.

- Ensure ViewModels and collectors have correct lifecycle ownership on Android

  and iOS.

- Do not assume a particular AndroidX ViewModel or lifecycle API is available

  on every configured target.

- Preserve cancellation. Do not convert `CancellationException` into an ordinary

  user-facing error or swallow it unintentionally.

- Define an overlapping-request policy for relevant operations: allow concurrent

  requests, cancel the previous request, ignore duplicates, or debounce inputs

  as appropriate.

- Handle stale responses when requests can complete out of order. An older

  response must not overwrite newer state when the use case requires

  latest-result semantics.

- Use `Mutex`, serialized processing, request identifiers, or other synchronization

  mechanisms only when justified.

- Do not assume `_state.update` protects a multi-step workflow or prevents

  duplicate business operations.

- Ensure collectors are stopped or cancelled according to the lifecycle and

  resource requirements of their host.

## Error handling and business operations

- Distinguish domain failures, network failures, authentication failures, and

  cancellation when those distinctions affect behavior.

- Do not silently swallow exceptions or expose raw technical exception messages

  directly to users.

- Keep domain errors independent of platform-specific UI frameworks.

- Let UseCases enforce business rules and coordinate domain operations; let

  ViewModel map relevant outcomes into presentation state.

- Define retry behavior and prevent unintended duplicate side effects.

- For important write operations, consider idempotency, server confirmation,

  persistence, and recovery after app restart.

- Do not duplicate business validation across ViewModel, UseCase, and Repository.

  Place each rule at the layer that owns it, while allowing UI-level validation

  for immediate user feedback.

## Platform boundaries and UI integration

### Shared code

- Keep portable domain rules, repository contracts, use cases, and state

  transitions in `commonMain` when they do not depend on platform APIs.

- Define interfaces for platform capabilities required by shared code.

- Avoid platform-specific types in shared domain contracts.

- Use the project's established resource and localization strategy.

- Do not introduce `Context`, Hilt, `R.string`, UIKit, SwiftUI, or platform-only

  APIs into shared business logic.

### Android

- Follow the existing Fragment, Compose, or hybrid UI approach.

- Collect state using the established lifecycle-aware mechanism.

- Do not collect UI state indefinitely from an inactive screen unless required.

- Render from state instead of keeping duplicate mutable UI state unnecessarily.

- Keep navigation and UI-only operations at the appropriate host or presentation

  boundary.

- Keep Hilt in the Android integration layer unless the project already uses

  a compatible shared DI solution.

### iOS

- Follow the existing SwiftUI, UIKit, or Compose Multiplatform host.

- Provide an explicit bridge for observing shared state when required.

- Ensure observation is stopped or cleaned up appropriately.

- Respect Swift concurrency and Kotlin coroutine cancellation boundaries.

- Do not assume Kotlin generic types, sealed classes, Flow, or coroutine APIs

  map directly to ergonomic Swift APIs; follow existing interop conventions.

- Keep platform-only navigation and presentation logic in the iOS host when

  appropriate.

## UI performance and collection handling

- Do not assume a large `UiState` is automatically a performance problem.

- Do not assume every state update causes the entire UI to redraw; behavior

  depends on the UI framework, observation boundaries, parameter stability,

  and rendering implementation.

- Reuse unchanged state branches when possible.

- Avoid unnecessary collection transformations and repeated expensive calculations.

- For large lists, use the project's established paging, diffing, lazy rendering,

  or incremental update mechanisms where appropriate.

- Avoid rebuilding or submitting identical lists unnecessarily.

- Do not add complex memoization, custom diffing, or multiple state streams

  without a demonstrated need.

- Keep performance optimizations proportional to the feature's actual complexity.

## Preserve the existing project

Before implementing or refactoring:

1. Read the relevant feature and adjacent implementations.

2. Identify existing naming conventions, state contracts, coroutine scopes,

   repository interfaces, UseCases, DI, navigation, and platform bridges.

3. Reuse existing abstractions when they meet the requirements.

4. Avoid introducing new base classes, generic frameworks, reducers, or

   dependency changes without a concrete benefit.

5. Do not change public APIs or business behavior unless the task requires it.

6. Do not upgrade Kotlin, Gradle, Compose Multiplatform, or related dependencies

   as an incidental part of an MVI change.

7. Prefer the smallest coherent change that follows the existing architecture.

## Definition of Done

A feature or refactor is complete when:

1. State transitions are consistent and published state is not mutated afterward.

2. ViewModel, UseCase, and Repository responsibilities remain clearly separated.

3. Coroutine ownership, cancellation, and overlapping-request behavior are appropriate.

4. Loading, errors, and important business outcomes are handled correctly.

5. Effects have appropriate delivery semantics.

6. Platform-specific APIs remain outside shared business logic.

7. Existing toolchain, dependencies, public APIs, and unrelated behavior remain
   unchanged unless the task requires a change.

8. Meaningful state and domain behavior has portable tests where the project has
   test infrastructure; platform bindings are checked on both hosts when changed.

9. Any claim that an Android or iOS app runs is backed by an actual host build or
   runtime check. If the task is static review, report that limit without building.

10. No abstraction or optimization has been added without a concrete reason.
