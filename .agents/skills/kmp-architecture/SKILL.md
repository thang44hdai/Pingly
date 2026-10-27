---
name: kmp-architecture
description: Design or review Clean Architecture for Kotlin Multiplatform apps targeting Android and iOS, including source sets, dependency direction, repositories, use cases, platform services, DI, and native hosts. Use when structuring a KMP project or adding a feature across both platforms.
---

# KMP Clean Architecture

## Start with the project

Inspect settings, Gradle plugins and version catalog, source sets, existing Android
and iOS hosts, framework integration, DI, navigation, tests, and working conventions.
Preserve a compatible setup. For a new app, shared Compose Multiplatform UI with
small native hosts is a useful default; honor requests for SwiftUI or native UI.
Do not copy Gradle DSL or dependency versions from another plugin generation.

## Dependency direction

```text
Android host / iOS host
        ↓
presentation (shared MVI and optional shared Compose UI)
        ↓
domain (models, repository contracts, meaningful use cases)
        ↑
data (repository implementations, mappers, data sources)
        ↓
platform services (network engine, database driver, permissions, files)
```

- Keep portable domain rules, models, repository interfaces, and use cases in
  `commonMain`. Domain depends on Kotlin and domain contracts, not on UI, DI,
  database, transport DTOs, or platform types.
- Put portable repository implementations, mapping, and data sources in
  `commonMain` when their dependencies support every configured target. Put
  platform implementations in `androidMain` or `iosMain` only when needed.
- Repositories expose domain models and domain-appropriate outcomes; map DTOs and
  database records inside data. Preserve coroutine cancellation. If wrapping an
  operation in `Result` or `runCatching`, rethrow `CancellationException` before
  mapping failures.
- A use case owns a meaningful business rule or orchestration step and depends on
  domain contracts. Do not create one-line forwarding use cases automatically.
  When a feature calls for UseCase-based Clean Architecture, presentation calls
  use cases rather than data sources or concrete repository implementations.
- Keep portable presentation state, events, effects, and state transitions in
  `commonMain` when using shared MVI. A compatible multiplatform Lifecycle
  ViewModel may also live there; `androidx.lifecycle.ViewModel` is not inherently
  Android-only when supplied by the multiplatform artifact. Follow the selected
  library's ownership rules on both hosts.
- Shared Compose screens, themes, and resources may live in `commonMain`.
  Android Activity/Context and Apple UIKit/SwiftUI entry points remain in their
  platform hosts or source sets. Platform-native UI is also valid when requested.

## Platform boundaries

Use `expect`/`actual` for a small platform capability with one implementation per
target when that keeps the API clear. Use an interface and constructor injection
when multiple implementations, fakes, or runtime selection matter. Neither tool
should leak platform types into domain contracts. Keep Android `Context`, Hilt,
`R`, Java-only APIs, UIKit, and SwiftUI out of portable business code.

Do not infer a shared mutable `object` is unsafe merely because it targets
Kotlin/Native. Assess ownership and synchronization for the actual concurrency
model. Avoid global mutable business state when a scoped, injected owner is clearer.

## Modules, hosts, and dependency injection

Begin with the project's existing modules. A small app can use `:shared` for
portable layers plus `:androidApp` and an Xcode `iosApp` host. Add feature or core
Gradle modules when boundaries, team ownership, or build size justify them. An
Xcode project is not included in `settings.gradle.kts` unless it is separately
configured as an actual Gradle module.

For AGP 9 projects using the Android KMP library plugin, preserve the project's
`com.android.kotlin.multiplatform.library` setup and its `kotlin { android { ... } }`
DSL. Configure only the Apple targets needed by supported hosts; Apple Silicon
simulators use `iosSimulatorArm64`, devices use `iosArm64`, and Intel simulator
support needs `iosX64`. Package a framework for the Xcode host using the project's
existing direct integration, CocoaPods, or SPM approach. Check current official
compatibility guidance before changing Kotlin, Compose, AGP, Gradle, or Xcode.

Constructor injection is enough for a small feature. Reuse existing Koin or
another compatible DI solution when present; do not add a framework solely to
illustrate layers. Compose the graph once per application, supply platform
implementations on each host, and ensure both hosts can resolve every required
contract. Keep ViewModel ownership tied to a real screen or navigation scope.

## Feature completion

For an implemented cross-platform feature, check the complete path on both hosts:
UI event → state holder → use case → repository contract → implementation → data
source, with the result mapped back to renderable state. Test meaningful domain
rules and MVI transitions in `commonTest` with fakes, including cancellation and
overlapping requests where relevant. Check platform bindings and host integration
separately. Report Android and iOS build/runtime evidence only for checks actually
performed; static review alone cannot establish that either app runs.

For detailed shared state and lifecycle rules, use
[kmp-mvi](../kmp-mvi/SKILL.md). For shared UI, use
[kmp-compose-performance](../kmp-compose-performance/SKILL.md). Use
[jetpack-compose-performance](../jetpack-compose-performance/SKILL.md) for
Android-specific performance diagnosis, not as a source-set architecture guide.

## Official references

- [KMP compatibility](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Android KMP library plugin](https://developer.android.com/kotlin/multiplatform/plugin)
- [Direct Xcode integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)
- [Multiplatform ViewModel](https://kotlinlang.org/docs/multiplatform/compose-viewmodel.html)
