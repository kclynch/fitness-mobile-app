# 90 Day Fitness Challenge

A native Android app for running a personal 90-day fitness challenge: pick a
start date, watch the countdown, and check off a daily to-do list you fully
control (add, edit, remove items) for any of the 90 days.

## Features

- **Setup screen** — pick a start date; the 90-day end date is computed
  automatically. Seeds a starter checklist (workout, water, diet, reading,
  progress photo) that you can freely edit.
- **Countdown card** — "Day X of 90" with a progress bar and days remaining,
  shown on the home screen.
- **Day view** — a horizontally scrollable day picker (1–90). Each day shows
  a completion dot once you've checked off items for it. Tap any day to view
  and check off that day's list; jump back to today with one tap.
- **Checklist management** — add, edit (rename), and remove items from the
  daily checklist via the FAB and per-row edit/delete icons. Changes apply
  to every day going forward (and back), while your per-day completion
  history is preserved independently.
- **Change start date** — adjust the challenge's start date later from the
  top bar menu if you need to.

## Tech stack

- Kotlin, Jetpack Compose (Material 3)
- Room (local SQLite persistence, via KSP)
- Single-`AndroidViewModel` architecture, no backend — all data stays on
  device

## Project structure

```
app/src/main/java/com/kclynch/fitness90/
  data/            Room entities, DAOs, database, repository
  ui/setup/        First-run start-date picker
  ui/home/         Home screen + ChallengeViewModel (countdown/day logic)
  ui/components/   Reusable Compose pieces (countdown card, day picker,
                   checklist row, add/edit/delete dialogs)
  ui/theme/        Material 3 theme
  MainActivity.kt  Switches between Setup and Home based on saved state
```

## Building

This project was developed in a sandboxed environment without an Android
SDK or network access to Google's Maven repository, so it has **not** been
compiled here. To build and run it:

1. Open the project root in Android Studio (Koala/Ladybug or newer).
2. Let Gradle sync — it will pull down the Android Gradle Plugin, Kotlin,
   AndroidX, Compose, and Room artifacts from Google's/Maven Central's
   repositories (`google()` / `mavenCentral()`, already configured in
   `settings.gradle.kts`).
3. Run the `app` configuration on an emulator or device (minSdk 26 /
   Android 8.0+).

Or from the command line, once the Android SDK is installed and
`local.properties`/`ANDROID_HOME` is set:

```
./gradlew assembleDebug
```

The Gradle wrapper (`gradlew`, `gradle/wrapper/`) is already included and
pinned to Gradle 8.7.
