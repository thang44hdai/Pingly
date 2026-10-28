# Pingly

Kotlin Multiplatform starter with a shared Compose UI. Home is the first screen;
it uses the Ping domain feature to create and display messages.
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

Flow: `HomeScreen` → `HomeEvent` → `HomeViewModel` → Ping use case →
`PingRepository` → `InMemoryPingRepository` → `StateFlow<HomeUiState>` → screen.

Koin starts in `PinglyApplication` on Android and via the Swift-facing
`initKoinIos()` call in `iOSApp.init()` on iOS. The
`HomeRoute` retrieves its ViewModel with `koinViewModel()` and uses `MviScreen`
to collect state with the screen lifecycle. The repository
keeps data only in memory, so Pings disappear when the process ends. Replace the
binding in `pingModule` with a persistent implementation when the product needs
storage; retain the domain contract and shared presentation flow.
The shared resources contain English and Vietnamese strings.

`commonTest` covers validation, feed order, repository snapshots, DI wiring, and MVI state
transitions. Build and runtime checks for Android and iOS remain necessary before
claiming either app runs; this scaffold has not been built as part of its generation.

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
