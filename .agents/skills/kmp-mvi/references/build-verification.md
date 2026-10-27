# Android and iOS build guidance

These checks are guidance for build-related work, not mandatory completion gates
for every MVI feature. Choose checks appropriate to the task.

## Discover before executing

Use the project's Gradle wrapper. Inspect settings, version catalog, wrapper,
plugins and CI. Run `./gradlew projects` and module `tasks --all` to discover actual
tasks rather than assuming :shared or :androidApp exists. Record Kotlin, Compose,
AGP, Gradle, JDK, Android SDK, Xcode, and deployment targets; verify the complete
combination against official compatibility tables. Do not automatically replace
working versions with an unrelated example's version table.

Check that the tracked Unix Gradle wrapper has executable permission before
recommending `./gradlew` or relying on an Xcode build phase that invokes it.
`bash gradlew` can aid local task discovery, but it does not fix a direct Xcode
integration that calls `./gradlew`.

On macOS check `xcodebuild -version`, `xcode-select -p`, and
`xcrun simctl list devices available`. A full selected Xcode installation, accepted
license, iOS SDK/runtime, and compatible simulator are required. Android needs the
selected SDK, build tools, and JDK. Linux/Windows may validate Android but cannot
satisfy the iOS application gate locally; use existing macOS CI or report it pending.

## Example checks

The following are examples, not universal task names. Substitute discovered module,
task, scheme, workspace, and destination values before running them.

| Gate | Example | Evidence |
|------|---------|----------|
| Android application | `./gradlew :androidApp:assembleDebug` | Successful task and APK |
| Android shared tests | Discovered shared Android host/unit test task | Passing test report, including commonTest |
| iOS simulator framework | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` | Successful native link and framework |
| iOS device framework | `./gradlew :shared:linkDebugFrameworkIosArm64` | Successful device native link |
| iOS shared tests | `./gradlew :shared:iosSimulatorArm64Test` | Passing native test report |
| iOS application | Xcode simulator build below | Successful build and .app |

For example, after discovering a real scheme and simulator UDID:

```bash
xcodebuild -list -project iosApp/iosApp.xcodeproj
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp -configuration Debug \
  -destination 'platform=iOS Simulator,id=<available-simulator-UDID>' \
  -derivedDataPath build/ios-derived-data \
  CODE_SIGNING_ALLOWED=NO build
```

Use `-workspace` instead of `-project` for a workspace-based integration such as
CocoaPods. Do not run this example with placeholder values. Confirm the Swift host
actually imports and embeds the newly built framework, rather than a stale binary.
Direct integration invokes `embedAndSignAppleFrameworkForXcode` from an Xcode build
phase before Swift compilation; do not invoke that task as a standalone terminal
build without the Xcode environment. Preserve CocoaPods/SPM integration if already
in use. For Swift export use the integration tasks supported by that project.

Verify framework name/module import, search paths, deployment targets, architectures,
and static/dynamic embedding settings. A successful framework link cannot catch
Swift host compile errors or missing framework embedding.

## CI and troubleshooting

Use the repository's existing CI conventions. Android jobs can run on Linux;
iOS link, simulator tests and Xcode build require macOS with a compatible selected
Xcode and simulator runtime. Select CI checks appropriate to the project and requested change. Keep signing credentials out of simulator gates.
Add release/archive checks only when release readiness is requested.

If a gate fails, fix the relevant code/configuration and rerun the failed gate plus
checks affected by that fix. Do not disable targets, exclude failing source sets,
skip tests, add empty actual implementations, or accept stale artifacts to obtain
green output. A no-source or skipped test task is not proof that shared tests ran.


## Official sources

- [KMP compatibility guide](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Direct Xcode integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)
- [Build and run KMP](https://kotlinlang.org/docs/multiplatform/build-and-run-kmp.html)
