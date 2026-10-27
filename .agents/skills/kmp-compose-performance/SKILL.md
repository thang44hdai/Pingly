---
name: kmp-compose-performance
description: >
  Create and review shared Compose Multiplatform UI for Kotlin Multiplatform
  projects targeting Android and iOS. Use when porting Android composables to
  commonMain, building shared screens, fixing safe-area or keyboard issues,
  optimizing recomposition, adding animations, or auditing cross-platform UI
  resources, lifecycle, and accessibility.
---

# KMP Compose — Android and iOS

Use stateless UI, state hoisting, intentional Modifier ordering, stable list
identity, and measured performance. Apply the platform guidance here to shared
Compose UI. Use [kmp-architecture](../kmp-architecture/SKILL.md) for layer and
source-set boundaries; use [jetpack-compose-performance](../jetpack-compose-performance/SKILL.md)
only for Android-specific performance diagnosis. Android runtime and profiling
rules do not establish iOS behavior.

## Start with the project

Inspect source sets, Compose/Kotlin plugins, version catalog, theme, navigation,
resources and native hosts. Preserve working UI and dependency choices. This skill
covers shared Compose UI; it does not require replacing an existing SwiftUI app.
Use [kmp-mvi](../kmp-mvi/SKILL.md) when shared business state or event handling needs
architecture work, rather than introducing a second state container in the UI.

Keep shared screens, UI models, themes and animations in commonMain. Activity,
Context, Android R resources and platform view integrations stay in androidMain;
UIKit and UIViewController integrations stay in iosMain. Prefer injected callbacks
or platform interfaces for permissions, file pickers, sharing and haptics.

For setup changes, verify the selected Kotlin, Compose Multiplatform, AGP, Gradle
and Xcode compatibility using official documentation. The Compose compiler plugin
must match Kotlin. Android's Compose BOM does not establish iOS compatibility;
check that every shared dependency provides the required native variants. Do not
invent version pins or upgrade a working project just to add a screen.

## Authoring contract

- Screens receive immutable state and event callbacks. Keep business logic out of
  composition; remember local UI state and use rememberSaveable only with supported
  saveable values. It is not a general cross-platform persistence mechanism.
- Put `modifier: Modifier = Modifier` first among optional parameters, after required
  parameters; trailing composable content slots may follow it. Apply the incoming
  modifier once to the component root. Choose padding, clipping, click and semantics
  order according to the intended drawing, hit area and accessibility behavior.
- Use MaterialTheme tokens and a shared theme; Material 3 is a design choice, not
  automatic native iOS styling. Isolate Android dynamic-color APIs behind the host.
- Use generated Compose resources and `org.jetbrains.compose.resources.stringResource`
  in shared UI, not Android R/stringResource. Localize labels, accessibility text,
  errors and plurals. Use shared painter/font resources or a compatible image loader.
- Use lifecycle-aware Flow collection with the selected multiplatform Lifecycle
  runtime. Own ViewModels at the screen/navigation scope; do not recreate them on
  recomposition. Run IO and effects in explicitly owned coroutines, never during
  composition. Key LaunchedEffect/DisposableEffect to the resource being owned,
  release listeners, and use rememberUpdatedState when callbacks must stay current.
- Give lazy items stable unique keys; provide contentType for different row layouts.
  Avoid unbounded nested scrolling in the same direction. Hoist sorting/filtering
  or remember it with correct inputs. Do not remember callbacks with incomplete
  keys that capture stale state or handlers.
- Annotate @Immutable/@Stable only when their contracts hold. Immutable snapshots
  and compiler inference are preferable to falsely annotating mutable objects.
  Use derivedStateOf when inputs change more often than the UI result, not as a
  blanket wrapper for every calculation. Defer high-frequency animation/scroll
  reads to layout/draw where useful; measure before optimizing.

Load supporting details directly when relevant:

| Reference | Use for |
|-----------|---------|
| [Platform UI](references/platform-ui.md) | Insets, resources, lifecycle, hosts, keyboard, native integrations |
| [Quality and review](references/quality-and-review.md) | Accessibility, animation, performance, screen audits and verification |


## Performance-first authoring rules

Optimize for correctness and avoid unnecessary work first; do not claim a change is
faster without measurement. When profiling is unavailable, identify likely risks and
explain the reasoning rather than presenting them as measured regressions.

- Keep composables cheap and side-effect free. Do not perform I/O, blocking work,
  state mutation, expensive transformations, or object creation with significant cost
  directly in the composable body when the work can be moved to an appropriate layer.
- Read state as close as practical to where it is used. Avoid making a large parent
  observe fast-changing scroll/animation state if only a small child needs it. For
  high-frequency values, consider deferring reads to layout/draw when supported and
  appropriate.
- Use `remember` only when it avoids meaningful repeated work or preserves UI state;
  include every changing input in its keys. Do not wrap trivial calculations or stable
  callbacks in `remember` automatically. Use `rememberUpdatedState` for long-lived
  effects that need the latest callback/value without restarting the effect.
- Use `derivedStateOf` when source state changes more frequently than the derived UI
  result needs to change. It is not a default wrapper for ordinary derived values.
- Prefer immutable UI state and narrow parameters. Avoid unnecessary whole-screen
  state coupling that makes unrelated sections recompose together; split state only
  when it improves ownership or update isolation, not just to maximize fragmentation.
- For lazy layouts, provide stable unique keys when items have identity, use
  `contentType` for heterogeneous rows when useful, avoid expensive per-item work,
  and avoid eagerly building a large list of composables. Keep nested same-direction
  scrolling bounded and intentional.
- Avoid unstable or mutable parameter contracts that defeat Compose's ability to skip
  work. Add `@Stable` / `@Immutable` only when the type truly satisfies the contract;
  annotations are not performance decorations. Do not assume every collection is
  unstable in every compiler version—inspect the project's compiler/tooling where
  the distinction matters.
- Cache expensive drawing calculations only when inputs and invalidation are correct.
  Use `drawWithCache` for cacheable drawing setup, not as a blanket wrapper. Avoid
  rebuilding large paths, brushes, formatters, or other expensive objects every frame
  when their inputs have not changed.
- Keep image requests appropriately sized and use a KMP-compatible image loader.
  Avoid decoding oversized images for small UI slots, duplicate requests, and unstable
  request parameters that cause unnecessary reloads.
- Preserve accessibility, animation correctness, state ownership and cross-platform
  behavior while optimizing. Do not remove semantics, skip lifecycle handling, or use
  platform-only APIs in commonMain to gain a speculative performance benefit.
- Prefer the simplest implementation that meets the need. Do not introduce custom
  layouts, caches, derived state, extra flows, wrappers, or abstractions without a
  concrete reason.

## Review mode

When asked to audit/review a screen, inspect its callers, source set and dependency
availability before proposing a fix. Report actionable findings with file/line,
impact, and a minimal correction. Use this checklist; mark unavailable runtime
observations as unverified rather than inventing failures:

| # | Check |
|---|-------|
| 1 | Modifier contract, hit areas and semantics order |
| 2 | Hoisted state, local UI state ownership and immutable values |
| 3 | Stable list identity, content types and bounded scrolling |
| 4 | Lifecycle collection, correct effect keys and disposal on both hosts |
| 5 | No IO, state writes or platform-only APIs during shared composition |
| 6 | Truthful stability contracts; unnecessary recomposition, allocations and expensive work are avoided without speculative optimization |
| 7 | Localized shared resources, Android insets and iOS safe areas/keyboard |
| 8 | TalkBack/VoiceOver semantics, scalable text and accessible interactions |

Modifier need not be the last optional parameter, missing stability annotations
alone are not defects, and a modern multiplatform Lifecycle runtime can support
collectAsStateWithLifecycle in commonMain. Do not import Android-only
recommendations into shared UI without checking native support and behavior.

Verification is proportional to the change. Build and runtime checks are useful
when available, not mandatory completion gates for every UI task. State which
platforms were exercised; never claim runtime success based only on a preview.
