# Pingly

Kotlin Multiplatform app with a shared Compose UI. Home implements the latest
Google Stitch “Pingly - Home & Emotion Orb” design with a partner portrait,
animated presence, mood insights, an emotion picker, Calendar, and Moments.
Android uses `androidApp`; iOS uses the Xcode project in `iosApp`. Both call the
shared `App` composable in `shared`.

## Structure

| Layer | Location | Responsibility |
|---|---|---|
| Domain | `shared/src/commonMain/.../feature/ping/domain` | `Ping`, repository contract, creation rules, feed ordering |
| Data | `shared/src/commonMain/.../feature/ping/data` | In-memory repository implementation |
| MVI base | `shared/src/commonMain/.../core/mvi` | State/event contracts, ViewModel state holder, lifecycle-aware screen collector |
| Presentation | `shared/src/commonMain/.../feature/home/presentation` | Home state, events, shared ViewModel |
| UI | `shared/src/commonMain/.../feature/home/ui` | Home route and stateless Compose screen |
| DI | `shared/src/commonMain/.../di/Koin.kt` | Koin modules, Repository/UseCase/ViewModel bindings |
| Hosts | `androidApp`, `iosApp`, `shared/src/iosMain` | Platform lifecycle and Compose entry points |

Home currently runs entirely on local UI state: `HomeScreen` emits `HomeAction`
to `HomeRoute`, which owns saveable emotion selection, navigation, settings,
sent-orb history, and calendar notes. Tap or long-press the portrait to simulate
sending an orb, or use the Hug Orb suggestion. The + button opens the Stitch emotion
picker with eight effects, animated phone preview, an overlay toggle, and a 1–5 second
duration slider. Its send button confirms a local preview and records the selected
effect in Moments without closing the picker. Calendar supports month navigation,
day selection, and adding notes. Settings control haptics and an in-app overlay
preview; no system overlay or server delivery is performed. Partner telemetry
and AI confidence are sample content. State survives UI recreation through
`rememberSaveable`; it is not durable storage.

The original Ping domain, in-memory repository, MVI base, and HomeViewModel remain
available for later data integration. Koin starts in `PinglyApplication` on Android
and via `initKoinIos()` in `iOSApp.init()` on iOS; the current Home route does not
instantiate that ViewModel. Existing `commonTest` coverage concerns the original
Ping/MVI flow. Shared resources include English and Vietnamese strings, bundled
Plus Jakarta Sans, Material Symbols, and the portrait from Stitch. Asset licenses
are listed in `shared/THIRD_PARTY_NOTICES.md`.

Android debug assembly, iOS simulator Kotlin compilation, and core UI interactions
on the Pixel 6a emulator have been verified. Running the full iOS app still requires
Xcode and a simulator runtime.

## Theme and language

`ui/theme/PinglyColorSchemes.kt` is the source for semantic color tokens. Its dark
palette matches the Stitch Luminous Intimacy design system; the light palette uses
the same roles. Screens use `MaterialTheme.colorScheme`, while `PinglyTheme.effects`
provides glass fills, optical highlights, image shading, and selected navigation
colors. Keep color literals in the token file when adding components.

Settings offers System / Light / Dark and System / English / Vietnamese. Changes
apply immediately to Home, sheets, Calendar, Moments, feedback messages, and system
bar appearance. Theme/language preferences are persisted using SharedPreferences
on Android and NSUserDefaults on iOS. Home UI state stays outside the locale provider
so changing language preserves the current tab, emotion, history, and open sheet.
The native locale adapters follow the Compose Multiplatform resource environment
approach: https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html.

## Run and verify

- Android: run `androidApp` from the IDE, or use `./gradlew :androidApp:assembleDebug`.
- iOS: open `iosApp/iosApp.xcodeproj` in Xcode and run the `iosApp` scheme. Its
  build phase creates the shared Kotlin framework through the Gradle wrapper.
- Shared tests: discover the available tasks with `./gradlew :shared:tasks --all`,
  then run the Android host and iOS simulator test tasks supported locally.

The Gradle wrapper is executable in the repository so the Xcode build phase can
invoke it. iOS verification requires a selected full Xcode installation and a
compatible simulator runtime.

## GitHub Actions: APK (IPA temporarily disabled)

`.github/workflows/build.yml` runs automatically only on pushes to `master`
(including merges into `master`), not on pull requests, tags, or other branches.
Manual dispatch is available; the Android job also requires the `master` ref.
Android runs shared tests and builds an installable **debug APK** on Ubuntu.
The iOS job is temporarily disabled with `if: ${{ false }}` in the workflow.
Remove that condition to resume IPA builds. When enabled, iOS archives the device
app on macOS and packages an **unsigned IPA** by default.
Download the files from **Actions > Build APK & IOS > run > Artifacts**;
artifacts are retained for 14 days. An unsigned IPA must be signed before it can
be installed on an iPhone. This workflow does not publish to either store.

For APK builds, no repository secrets, signing keystore, or environment variables
are needed. Commit and push the workflow to `master`. In GitHub **Settings >
Actions > General**, ensure GitHub Actions and the actions used by this workflow
are allowed (`actions/*`, `gradle/actions`, and `android-actions/setup-android`).
The workflow needs only `contents: read`; no personal access token or write
permission is required. GitHub-hosted Ubuntu runners provide the build machine.

To export a signed IPA, configure **Settings > Secrets and variables > Actions**:

| Type | Name | Value |
|---|---|---|
| Secret | `IOS_CERTIFICATE_BASE64` | Base64 of an Apple Distribution `.p12`, including its private key |
| Secret | `IOS_CERTIFICATE_PASSWORD` | Password for that `.p12` |
| Secret | `IOS_PROFILE_BASE64` | Base64 of the matching distribution `.mobileprovision` |
| Variable | `IOS_TEAM_ID` | Apple Developer Team ID |
| Variable | `IOS_BUNDLE_ID` | App ID matching the profile; defaults to `com.thangnq.pingly.Pingly` |
| Variable | `IOS_EXPORT_METHOD` | `ad-hoc` (default) or `app-store`; must match the profile |

On a Mac, use `base64 -i certificate.p12 | pbcopy` and
`base64 -i profile.mobileprovision | pbcopy` to prepare the secrets.
Select **Run workflow**, enable **signed_ios**, and run against a trusted branch.
For ad hoc installation, the profile must include the receiving device's UDID.
An App Store IPA is for upload to App Store Connect, not direct installation.
Signing material is imported into a temporary keychain and removed after the build.
See [GitHub's signing setup](https://docs.github.com/en/actions/how-tos/deploy/deploy-to-third-party-platforms/sign-xcode-applications)
for exporting and storing the signing credentials.

The iOS bundle ID is overridden only in CI, so it does not inherit the local
`TEAM_ID` suffix. Android release signing and store uploads are not configured.
The first hosted run must verify dependency availability and Xcode compatibility;
the Windows development environment cannot execute the iOS archive/export steps.
