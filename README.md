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
