# Unloop

Unloop is an Android application that helps users reduce distractions by blocking selected apps during scheduled routines and instant focus sessions.

## Technologies

- Language: Kotlin
- UI: Jetpack Compose
- Database: Room.
- Preferences: DataStore
- Dependency injection: Hilt
- Architecture: MVI, Model-View-Intent
- Navigation: Navigation 3
- Date and time: kotlinx.datetime
- Design: Material Design 3 Expressive

## Project structure

The application has a single `app` module. Kotlin source files live under `app/src/main/java/com/charan/unloop`.

### Core

- `core/di`: Hilt modules for database access, repository and session bindings, and the application coroutine scope.
- `core/notification`: Notification creation and updates through `NotificationHelper`.
- `core/startup`: Reboot and app-update recovery through `BootCompletedReceiver`.
- `core/utils`: Permission handling, date and time helpers, and application utilities.

### Session

- `session`: Session coordination and Android control contracts through the `SessionManager` and `SessionController` interfaces, and foreground execution through `SessionForegroundService`.
- `session/impl`: Session coordination through `SessionManagerImpl` and alarm scheduling and service commands through `SessionControllerImpl`. Both implementations are bound as singletons in `SessionModule`.

### Blocking

- `session/blocking`: Foreground app detection through `AppUsageChecker` and blocking overlay execution through `FullScreenOverlayService`.

### Data

- `data/apps`: Installed app discovery through `InstalledAppsDataSource`.
- `data/local`: Local Data Source (Room Database: entities, DAOs, type converters, models for Room relations, entity related enums, and migrations).
- `data/model`: Data models other than local entities.
- `data/repository`: Repository interfaces.
- `data/repository/impl`: Repository implementations.

### Presentation

- Feature packages (e.g., `onboarding`, `settings`): Screens, ViewModels, State classes, Effect classes, Event classes, and specific mappers.
- `/common`: Shared and reusable UI components, and presentation-specific UI models.
- `/root`: Global application state and core navigation definitions.
- `/theme`: Material 3 design system configuration (colors, shapes, typography, theme definition).

## Development rules and conventions

### Data layer

- Put model conversions in dedicated mapper extension functions. Shared entity-to-UI and UI-to-entity conversions belong in `presentation/common/mapper`.
- When changing the database schema, increment the database version, provide the required migration, and add a migration test in `app/src/androidTest/java/com/charan/unloop/DBMigrationTests.kt`.

### Presentation layer

- Follow a strict MVI (Model-View-Intent) architecture for all features.
- One-time events (navigation, toasts, snackbars) must be handled via Effect, not State.
- Mapping from local data layer entities to UI-specific states should be performed using dedicated mapper functions.
- Presentation mappers must be placed under `presentation/common/mapper`.
- Feature-specific presentation packages must contain:
  - Screen composable (`*Screen.kt`)
  - ViewModel (`*ViewModel.kt`)
  - State class (`*State.kt`)
  - Effect class (`*Effect.kt`)
  - Event class (`*Event.kt`)
- Shared/reusable UI components must be placed under `presentation/common/components`.
- If any components from different features require similar UI models, they should be placed in `presentation/common/model`.
- ViewModels should use Hilt's `@HiltViewModel` and constructor injection for dependency injection.
- UI should be driven entirely by state.
- UI components must only consume UI models or UI states.
- Use Material 3 Expressive components and design patterns.
- Do not use `Card` components unless explicitly required by the design.
- When implementing a new feature, match the structure and patterns used by existing features.

### Sessions, scheduling, and app blocking

- Keep session coordination and execution in the top-level `session` package. Keep `NotificationHelper` under `core/notification` for application-wide notification support.
- Keep foreground app detection and blocking overlay execution in `session/blocking`. Keep reboot and app-update recovery in `core/startup`.
- Route all session events and scheduling operations through `SessionManager` so instant sessions, scheduled sessions, and notification actions follow the same logic.
- Scheduled alarms and notification actions send request intents to `SessionForegroundService`, which delegates them to `SessionManager`. The manager updates session data through repositories and sends command intents through `SessionController`; the service executes those commands.
- Use `SessionController` to send session control intents, such as start, pause, resume, and stop, and to schedule or cancel alarms through `AlarmManager`. Keep all related Intent and PendingIntent creation in this controller.
- Depend on the `SessionManager` and `SessionController` interfaces. Keep their implementations in `session/impl`.
- Put all date and time calculations in `DateTimeUtils`, including durations, start and end times, and upcoming schedule times. Use kotlinx.datetime for date and time operations.

### Naming conventions

- Screens: `*Screen`, such as `RoutineScreen`.
- ViewModels: `*ViewModel`, such as `HomeViewModel`.
- State classes: `*State`, such as `SettingsState`.
- Effects: `*Effect`, such as `RoutineEffect`.
- Events: `*Event`, such as `HomeEvent`.
- UI models: `*UiModel`, such as `RoutineUiModel`. Match Unloop's existing `UiModel` capitalization.
- Mappers: Extension functions with explicit conversion names, such as `RoutineEntity.toRoutineUiModel()`.
- Repository interfaces: `*Repository`.
- Repository implementations: `*RepositoryImpl`.
- DAOs: `*Dao`.
- Entities: `*Entity`.
- Feature packages: Lowercase names with underscores where already used, such as `all_routines` and `on_boarding`.

### General guidelines

- Keep presentation logic in ViewModels, data access logic in repositories, and session coordination in `SessionManager`. Keep composables focused on rendering state and dispatching events.
- Avoid unnecessary comments. Explain non-obvious platform constraints or decisions when a comment helps.
- Keep padding and spacing consistent with surrounding screens.
- Use Material theme typography instead of hardcoded font sizes.
- Define user-facing strings in Android string resources. Use `stringResource()` in composables and resource lookup outside Compose.
- Import classes and functions directly. Use fully qualified names only to resolve naming conflicts.
- Reuse existing helpers and components before introducing alternatives.
- Write tests only for database migrations.
- Do not launch the app in an emulator unless the task requires it or the user explicitly requests it.
