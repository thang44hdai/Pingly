# Cross-platform UI quality

## Accessibility and adaptive layout

Use standard Material/Foundation controls and semantics before custom gesture-only
widgets. Give actions accessible labels, roles, state and error descriptions.
Decorative imagery should not add duplicate announcements. Group related content
intentionally; do not clear semantics that carry useful actions or editable text.

Check TalkBack on Android and VoiceOver on iOS. Verify focus order, heading/group
announcements, toggle state, modal focus, and keyboard access where applicable.
Inspect semantics exposed by embedded native views as well as Compose nodes.
A passing shared semantics test does not establish correct screen-reader behavior.

Keep touch targets generous using standard component minimums and project/platform
accessibility guidance; do not shrink interactive bounds to the visible icon.
Support large font settings, RTL, long translations, light/dark themes and adequate
contrast. Avoid fixed text heights, clipped labels and color-only status signals.
Adapt to available window constraints and orientation rather than hardcoded device
names. Test phone and tablet layouts if the product supports them.

## Animation, drawing and performance

Prefer supported common animation APIs (animate*AsState, transitions, Animatable)
and Canvas. Keep animation objects remembered, scopes owned, and callbacks current.
Use frame timing rather than a fixed delay loop for custom continuous animation.
Avoid allocating large objects or rebuilding paths on every draw when inputs are
unchanged. Use drawWithCache only for work with appropriate cache dependencies.
Provide equivalent understandable state changes when motion is reduced, using
platform accessibility settings through a supported API or platform adapter.

Investigate actual frame issues before changing stability or caching. Measure a
representative list/animation on Android and iOS; rendering backends and platform
interop can produce different bottlenecks. Android baseline profiles and Android
profilers do not validate iOS performance. Use Xcode Instruments for Apple-side
issues and appropriate Compose tooling supported by the selected release.

## Compose performance review checklist

Review performance as part of ordinary code authoring; do not require a separate
profiling task for every small UI change. Inspect the changed composables and their
callers for these common risks:

1. **Recomposition scope:** Which state reads invalidate which composables? Keep
   rapidly changing scroll/animation state reads near the UI that needs them. Avoid
   passing unrelated fast-changing values through a large subtree.
2. **Work in composition:** Look for filtering, sorting, grouping, parsing, formatting,
   large allocations, resource loading, I/O or state writes in composable bodies.
   Move expensive work to the ViewModel/domain/data layer or cache it with correct keys
   where that matches ownership and semantics.
3. **State and effects:** Avoid creating jobs, flows, listeners or side effects during
   composition. Ensure effects have meaningful keys and clean up resources. Do not
   restart effects because a callback changed if `rememberUpdatedState` is appropriate.
4. **Memoization:** Check whether `remember`/`derivedStateOf` is actually useful, keyed
   correctly, and not hiding stale data. Remove redundant wrappers around cheap work.
5. **Stability:** Prefer immutable state and narrow parameters. Verify stability concerns
   before adding annotations or redesigning models; `@Stable` and `@Immutable` must be
   truthful. Compiler behavior can vary by Kotlin/Compose version.
6. **Lists:** Check stable unique item keys, suitable `contentType` for mixed item types,
   per-row work, eager materialization, nested scrolling and item state preservation.
7. **Drawing and animation:** Avoid expensive repeated path/object creation and work per
   frame. Cache only values whose inputs and invalidation are understood. Prefer the
   standard animation APIs where sufficient.
8. **Images:** Check request sizing, loader/platform compatibility, request stability,
   placeholders and avoid needless reloads or oversized decoding.
9. **Cross-platform reality:** A shared implementation can have different costs on
   Android and iOS due to rendering backend and native interop. Do not infer iOS results
   from Android profiling or vice versa.

### Evidence and prioritization

- Fix clear, avoidable work first. Prioritize by likely user impact, frequency, and cost.
- If there is no profiler/trace, label findings as potential risks based on code—not as
  measured slowdowns. Recommend profiling only when the issue is consequential or
  cannot be decided confidently by inspection.
- Do not optimize merely to reduce recomposition counts: recomposition is normal. Focus
  on expensive work, frame drops, excessive allocations, unnecessary I/O, and user-visible
  latency.
- Do not add `remember`, `derivedStateOf`, `@Stable`, caches, custom layouts or extra
  state streams automatically. Each optimization should have a clear reason and preserve
  correctness.
- In reviews, report each actionable finding with file/line when available, impact,
  confidence (observed issue vs potential risk), and the smallest reasonable fix.

## Task-sized verification

Choose checks based on the change and available environment:

- Shared UI tests for visible states, click → event, focus and semantics, using the
  test APIs/runners supported by the selected Compose Multiplatform version.
- Android and iOS host checks for first render, safe areas, keyboard, back gestures,
  dark mode, localization, font scaling and native view interoperability.
- Relevant compilation/application builds when imports, resources, dependencies,
  host integration or platform implementations change.

Keep deterministic presentation tests separate from platform interaction checks.
Do not add brittle screenshots for every small cosmetic change. If screenshot
coverage exists, account for platform fonts, rendering, locale and device settings.

Build checks are guidance, not an unconditional requirement to build both apps for
every composable edit. Report what was actually checked and any relevant limitation.
This repository contains skill documentation; validating it does not execute a
consumer project's Android or iOS UI.

## Official references

- [iOS accessibility](https://kotlinlang.org/docs/multiplatform/compose-ios-accessibility.html)
- [Compose lifecycle and identity](https://developer.android.com/develop/ui/compose/lifecycle)
- [Platform UI behavior](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
