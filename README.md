# Creamy

Creamy is a Kotlin Android MVP built with Jetpack Compose. It provides a simple calendar-based interface for creating, viewing, and managing records with local persistence.
This app helps users track when and where they use their creams. It is designed to improve skin care by making it easier to follow optimal routines.

## Features
- Calendar-style daily overview
- Add and edit records from the app UI
- Local database storage using Room
- Material 3 design with Compose
- Lightweight MVP structure for rapid iteration

## Tech Stack
- Kotlin
- Android Jetpack Compose
- Room
- ViewModel / Lifecycle
- Gradle + KSP

## Project Structure
- `app/` – Android app module
- `app/src/main/java/com/amirbatyrov/creamy/` – main app code
- `addRecord/` – record creation and editing screens
- `calendar/` – calendar and record display UI
- `db/` – Room entities, DAO, and database setup

## Getting Started
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Run the app on an emulator or Android device

## Requirements
- Android Studio
- JDK 11+
- Android SDK 26 or newer

## Status
This project is an MVP and is intended to demonstrate the core app flow and architecture. Going forward I plan to re-write it to use Kotlin Multiplatform to bring this app to all platforms.
