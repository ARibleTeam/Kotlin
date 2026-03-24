# AGENTS Project Map

This file is a quick navigation map for agents and maintainers.

## Root

- `settings.gradle.kts` - project name, module includes (`:app`), repositories.
- `build.gradle.kts` - root Gradle plugins (`apply false`).
- `gradle.properties` - global Gradle and Android build flags.
- `gradlew`, `gradlew.bat` - Gradle wrapper launch scripts.
- `gradle/libs.versions.toml` - centralized dependency and plugin versions.
- `gradle/wrapper/gradle-wrapper.properties` - pinned Gradle wrapper version.

## Main Module

- `app/build.gradle.kts` - app module config, dependencies, SDK/build types.
- `app/proguard-rules.pro` - ProGuard/R8 rules for release builds.

## Runtime Entry Points

- `app/src/main/AndroidManifest.xml`
  - registers `App` (`android:name=".App"`).
  - `MainActivity` is launcher (`MAIN` + `LAUNCHER`).
- `app/src/main/java/com/example/alexrosh/App.kt` - app-wide initialization.
- `app/src/main/java/com/example/alexrosh/MainActivity.kt` - first screen and primary navigation.

## Core Feature Files

- `app/src/main/java/com/example/alexrosh/SearchActivity.kt` - search screen logic and states.
- `app/src/main/java/com/example/alexrosh/ITunesApi.kt` - Retrofit interface for iTunes API.
- `app/src/main/java/com/example/alexrosh/TracksResponse.kt` - API response model.
- `app/src/main/java/com/example/alexrosh/Track.kt` - track domain model.
- `app/src/main/java/com/example/alexrosh/TrackAdapter.kt` - RecyclerView adapter for search results.
- `app/src/main/java/com/example/alexrosh/SearchHistory.kt` - local search history storage.
- `app/src/main/java/com/example/alexrosh/SettingsActivity.kt` - settings/actions screen.
- `app/src/main/java/com/example/alexrosh/LibraryActivity.kt` - media library screen.

## UI Resources

- `app/src/main/res/layout/activity_main.xml` - main screen layout.
- `app/src/main/res/layout/activity_search.xml` - search screen layout.
- `app/src/main/res/values/strings.xml` - text resources.
- `app/src/main/res/values/colors.xml`, `themes.xml` - app theming.

## Tests

- `app/src/test` - unit tests.
- `app/src/androidTest` - instrumentation/UI tests.

## Fast Orientation

1. Open `AndroidManifest.xml` to confirm runtime entry points.
2. Open `MainActivity.kt` to see navigation.
3. Open `SearchActivity.kt` to inspect core behavior.
4. Open `app/build.gradle.kts` for dependency/build context.
