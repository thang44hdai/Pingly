# Platform UI integration

## Native hosts and common UI

A shared App composable can be called by Android Activity.setContent and by
ComposeUIViewController in iosMain. Keep app initialization, DI and ViewModel
creation outside repeated composition. Use a real lifecycle/navigation owner on
each host. A preview is not an iOS application host.

For AGP 9 projects, the shared KMP library uses the Android KMP library plugin
and the Android app remains a separate application module. Preserve older compatible
layouts unless migration is requested. Use the project's existing Apple framework
integration (direct Xcode, CocoaPods or SPM); do not change packaging just for UI.
Configure device and simulator targets according to the supported hosts.

## Insets and keyboard ownership

Android's Activity enables edge-to-edge. Shared layout uses supported Compose
WindowInsets APIs, which CMP maps to iOS safe areas. Do not add fixed status-bar
heights or assume UIKit and Compose coordinate systems use identical units.

Choose one inset owner. Normally let Scaffold handle its contentWindowInsets,
apply innerPadding to content, and consume that padding before nested inset-aware
children. Do not combine full Scaffold padding with another safeDrawingPadding
without checking whether it doubles system padding. A full-bleed background can
extend under system UI while controls respect safe drawing bounds.

Handle IME padding and scroll-to-focused-field behavior at the appropriate content
container. Check parent inset consumption and UIKit host safe-area policy before
adding imePadding. Avoid applying both UIKit safe-area constraints and equivalent
Compose padding twice. Test the bottom action area, sheets, landscape, keyboard
opening/closing, and nested navigation layouts on both platforms.

Use focus/keyboard APIs supported by the project's CMP release. Choose keyboard
type and IME action appropriate to the field; preserve cursor/selection and avoid
replacing field state on every emission. Platform autofill, secure input and
keyboard behavior need platform checks instead of Android-only assumptions.

## Resources and theming

Store shared assets under src/commonMain/composeResources (values, drawable,
font, and localized qualifier directories). Use the project's generated Res package;
its namespace depends on configuration. Include imports for generated accessors
when required. Use Compose resources for strings, plurals, images and fonts.
Do not copy Android XML drawables or Android resource IDs into common code without
checking resource format support. Package resources through the existing iOS
integration and inspect the installed app when resources fail only on a device.

Share typography and semantic color tokens. Handle dark mode through the shared
theme and host settings. Android dynamic color is a platform capability: inject
a palette or supply a shared fallback. Verify custom fonts include needed glyphs
and locale coverage. Do not silently render platform-specific text as a bitmap.

For network images, reuse a KMP-compatible loader. With Coil 3, verify shared
Compose artifacts and a compatible networking implementation/engine for Android
and iOS. Keep Android ImageLoader factories and Context-based configuration out
of common code. Provide appropriate request sizes, placeholders, error states and
localized descriptions; use null descriptions for purely decorative images.

## Lifecycle and interop

Use compatible JetBrains multiplatform Lifecycle artifacts in commonMain, while
Kotlin imports remain androidx.lifecycle.*. Prefer collectAsStateWithLifecycle
where provided by the selected runtime. If unavailable, use an explicit lifecycle
adapter or a compatible dependency change; do not assume collectAsState stops in
the background. Lifecycle and composition lifetimes are different.

A lifecycle-aware effect collector should have one consumer per effect stream.
Define background delivery policy with the architecture layer. DisposableEffect
must detach native listeners and delegates when the native resource leaves scope.
Use AndroidView only in androidMain, UIKitView/UIViewController APIs in iosMain,
and a portable abstraction at the common boundary. API details vary by CMP version.
Check native view sizing, accessibility merging, focus, and gesture arbitration.

Keep navigation callbacks/routes portable; verify the chosen navigation library
supports both targets. Verify Android system back, iOS interactive back gestures,
and modal dismissal against the existing navigation model. Do not emulate native
behaviors with a universal Android BackHandler import in commonMain.

## Cross-platform performance notes

Keep the shared implementation efficient without assuming Android and iOS have
identical rendering or interop costs:

- Keep expensive work out of composition and avoid repeated conversions between
  Compose and native view representations. Use native interop only where it is needed.
- Keep image-loader setup and platform-specific engine configuration at the appropriate
  platform/application boundary; do not create an `ImageLoader` or other heavyweight
  service per composable.
- Avoid platform-specific performance hacks in `commonMain`. If evidence shows a
  platform-specific bottleneck, isolate the smallest necessary implementation behind
  `expect`/`actual` or an injected platform interface only when the project architecture
  already supports that approach.
- Validate lifecycle and disposal when native views, delegates, listeners or callbacks
  are involved; leaked interop resources can cause both correctness and performance
  problems.
- Do not claim cross-platform performance equivalence from shared source alone. Treat
  platform profiling as evidence for that platform, and keep speculative concerns labelled
  as such when profiling has not been done.

## Official references

- [Platform UI behavior and insets](https://kotlinlang.org/docs/multiplatform/compose-platform-specifics.html)
- [Resource setup](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-setup.html)
- [Using resources](https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html)
- [Lifecycle](https://kotlinlang.org/docs/multiplatform/compose-lifecycle.html)
- [Compose compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html)

Check APIs against the project's selected version before using examples from the
latest documentation.
