# Platform integration

## Source sets and hosts

```text
androidApp/                       Android application and manifest
shared/                          KMP library (name may differ)
  src/commonMain/kotlin/         MVI, domain, portable data; optional shared UI
  src/commonTest/kotlin/         reducers, ViewModel and repository tests
  src/androidMain/kotlin/        Android service implementations
  src/iosMain/kotlin/            Apple service implementations; Compose entry
  src/iosSimulatorArm64Test/     simulator-specific tests when needed
iosApp/                          Xcode application, Swift host, resources
```

For new Apple targets configure iosArm64 (devices) and iosSimulatorArm64 (Apple
Silicon simulator), including `binaries.framework` for both. Add iosX64 only when
Intel simulator support is required and supported by the selected toolchain.
Use the default source-set hierarchy so iosMain implementations reach every Apple
target. A directory alone does not declare a target.

For AGP 9, use `org.jetbrains.kotlin.multiplatform` with
`com.android.kotlin.multiplatform.library` in the shared library and a separate
`com.android.application` host. Do not apply Android application/library plugins
together with KMP as a blanket legacy recipe. Keep older working projects on their
compatible setup unless migration is part of the task. Inspect plugin-specific DSL
and Android test opt-ins; task names differ across plugin generations.

Compose modules need `org.jetbrains.compose` and
`org.jetbrains.kotlin.plugin.compose`; the compiler plugin version must match
Kotlin. Android Compose BOM alignment does not establish commonMain/iOS artifact
compatibility. Verify each dependency publishes all requested KMP variants.

## Shared presentation

Use JetBrains multiplatform Lifecycle/ViewModel artifacts in commonMain, e.g.
`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` and
`org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose`, at versions supported
by the project. Kotlin imports remain `androidx.lifecycle.*`.

Modern CMP lifecycle supports lifecycle-aware collection in common code. Use
`collectAsStateWithLifecycle()` when provided by the selected runtime; do not copy
the older repository reference's blanket claim that it is Android-only. If the
existing version lacks the API, use an explicit lifecycle-aware adapter or a
compatible dependency update, and verify background behavior on both hosts.

Create ViewModels with an explicit factory/initializer and a real screen or
navigation ViewModelStoreOwner, including on iOS. Do not instantiate a ViewModel
on every recomposition or use reflection-only construction on Kotlin/Native.
Collect transient effects in a lifecycle-aware coroutine with one consumer,
`LaunchedEffect(viewModel, lifecycleOwner)` and `repeatOnLifecycle(STARTED)` when
supported. Use updated callbacks to avoid stale navigation handlers.

Shared Compose UI uses `org.jetbrains.compose.resources.stringResource` with
generated `Res` values. Map shared error codes to localized text at the UI boundary.
Keep Android `R`, `@StringRes`, Context, Hilt annotations, java.* and UIKit out of
commonMain. UIKit belongs in iosMain. Use platform interfaces or matching
expect/actual declarations for capabilities; never leave TODO actuals.

Use a stateless Screen(state, onEvent, modifier), stable lazy-list keys, and shared
safe-area/window insets. Android's Activity calls enableEdgeToEdge and setContent;
iOS exposes a ComposeUIViewController from iosMain to the Swift host. Verify
keyboard and system-bar padding on both, avoiding duplicate inset application.
Keep navigation routes and callbacks portable; do not force an Android-only
navigation artifact into commonMain.

## SwiftUI host alternative

Keep the same shared MVI contract when the project uses native UI. Use an existing
maintained Flow/Swift observation bridge compatible with the toolchain, or provide
an explicit adapter with a cancellable scope. SwiftUI must own the adapter and
ViewModel lifecycle; cancel observation and clear the store on final disposal.
Handle Main-thread updates, errors, Swift-exported API types, and effect ownership.
StateFlow does not automatically become ObservableObject or AsyncSequence.

## Platform data and dependency injection

Constructor injection is sufficient. Reuse an established KMP DI framework when
present; initialize it once per application and register both platform bindings.
Android Hilt can supply dependencies to a shared constructor through a factory.
Verify iOS startup resolves the same required contracts.

Inject Ktor clients with platform engines (for example OkHttp in androidMain and
Darwin in iosMain). Configure platform database paths, drivers/KSP targets and
permissions on both platforms. Do not add network/storage libraries solely to
illustrate MVI. Retain the project's existing working implementations.

## Official sources

- [AGP 9 migration](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html)
- [Android KMP library plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- [Multiplatform ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html)
- [Multiplatform lifecycle](https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html)
- [Compose compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)

Recheck these for the selected project versions; do not copy whichever version
happens to appear in the latest documentation.
