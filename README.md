# Pratham Chikitse (ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ)

> **Offline-First First Aid Emergency Guide** — Android application providing step-by-step first aid instructions for 36+ emergency situations in English, Kannada, and Hindi.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Screens](#screens)
- [Dependencies](#dependencies)
- [Build & Run](#build--run)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Pratham Chikitse (First Aid in Kannada) is an offline-first Android app designed to help users respond to medical emergencies with confidence. It provides clear, step-by-step guidance for 36+ emergencies, works entirely offline, supports three languages, and includes Text-to-Speech voice guidance — making it accessible even in low-literacy or panic situations.

**Key highlights:**
- Works completely offline — no internet required for core features
- Trilingual: English, Kannada (ಕನ್ನಡ), and Hindi (हिन्दी)
- Voice-guided first aid steps via Text-to-Speech
- SOS dialer (112) with quick access to emergency numbers (108, 100, 101)
- Nearby hospital locator with GPS-based distance sorting
- Rule-based offline AI chat assistant for first aid queries
- Dark mode support
- Accessible design with configurable font size

---

## Features

| Feature | Description |
|---|---|
| **Emergency Grid** | 36+ emergencies with icons, sorted by severity (Critical / Moderate / General) |
| **Search & Voice Search** | Full-text search across titles and descriptions in English & Kannada; Google voice input |
| **Category Filter** | Filter emergencies by severity using Material3 FilterChips |
| **Step-by-Step Guide** | Horizontal pager with Do's and Don'ts, animated transitions, large text |
| **Text-to-Speech** | Play / pause / stop / auto-advance; supports English, Kannada, Hindi |
| **SOS Dialer** | Pulsing 112-dial button with quick-dial cards for Ambulance (108), Police (100), Fire (101) |
| **Nearby Hospitals** | GPS-based location, Haversine distance calculation, sorted list with Call & Directions |
| **Offline Chat** | Rule-based first aid chatbot answering common queries (CPR, bleeding, burns, etc.) |
| **Settings** | Language switching, Dark Mode, TTS toggle & speed, Font size slider |
| **Recently Viewed** | Quick access to recently opened emergencies |
| **SOS Banner** | Home screen banner — long-press to directly dial 112 |
| **Multilingual** | Full UI and content in English, Kannada, and Hindi |

---

## Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Toolkit** | Jetpack Compose + Material3 |
| **Architecture** | Clean Architecture + MVVM |
| **Navigation** | Navigation Compose |
| **DI** | Dagger Hilt |
| **Database** | Room (SQLite) |
| **Preferences** | DataStore |
| **Async** | Kotlin Coroutines + Flow |
| **Serialization** | Kotlinx Serialization (JSON) |
| **Maps / Location** | Google Play Services Location, Maps, Places |
| **Testing** | JUnit 4, MockK, Turbine, Mockito |
| **Build** | Gradle 9.0 + AGP 8.5.2 + Kotlin 2.0.21 |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 35 |
| **Compile SDK** | 35 |

---

## Architecture

The app follows **Clean Architecture** with **MVVM** pattern:

```
┌─────────────────────────────────────────────────┐
│                  UI Layer                        │
│  Composables (Screens) ← → StateFlow ← → HiltViewModel │
└───────────────────────┬─────────────────────────┘
                        │ calls
┌───────────────────────▼─────────────────────────┐
│               Domain Layer                       │
│  UseCases → Repository Interfaces → Models      │
│  (Pure Kotlin, no Android dependencies)          │
└───────────────────────┬─────────────────────────┘
                        │ implements
┌───────────────────────▼─────────────────────────┐
│               Data Layer                         │
│  RepositoryImpl → Room DAO / DataStore / JSON Assets │
└─────────────────────────────────────────────────┘
```

**Key patterns:**
- **Single Activity** with Jetpack Navigation Compose
- **ViewModels** expose `StateFlow<UiState>` observed via `collectAsStateWithLifecycle()`
- **Repository pattern** — interfaces in domain, implementations in data, wired via Hilt `@Binds`
- **Use Cases** — single-responsibility classes with `operator fun invoke()`
- **Offline-First** — all data loaded from local Room DB (seeded from bundled JSON assets)
- **Singleton TTS Manager** with pause/resume/stop and utterance progress tracking

---

## Project Structure

```
PrathamChikitse/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/prathamchikitse/
│   │   │   │   ├── MainActivity.kt              # Single Activity (Compose host)
│   │   │   │   ├── PrathamApp.kt                 # @HiltAndroidApp
│   │   │   │   ├── navigation/
│   │   │   │   │   └── AppNavGraph.kt            # NavHost with 8 routes
│   │   │   │   ├── ui/
│   │   │   │   │   ├── splash/SplashScreen.kt
│   │   │   │   │   ├── onboarding/LanguageOnboardingScreen.kt
│   │   │   │   │   ├── home/HomeScreen.kt
│   │   │   │   │   ├── detail/EmergencyDetailScreen.kt
│   │   │   │   │   ├── chat/EmergencyChatScreen.kt
│   │   │   │   │   ├── hospitals/HospitalScreen.kt
│   │   │   │   │   ├── sos/SOSScreen.kt
│   │   │   │   │   ├── settings/SettingsScreen.kt
│   │   │   │   │   ├── components/EmergencyTile.kt
│   │   │   │   │   └── theme/{Color,Theme,Type}.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   ├── HomeViewModel.kt
│   │   │   │   │   ├── EmergencyDetailViewModel.kt
│   │   │   │   │   ├── HospitalViewModel.kt
│   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/Models.kt
│   │   │   │   │   ├── repository/Repositories.kt
│   │   │   │   │   └── usecase/UseCases.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── repository/{EmergencyRepositoryImpl,PreferencesRepositoryImpl}.kt
│   │   │   │   │   └── local/
│   │   │   │   │       ├── EmergencyDatabase.kt
│   │   │   │   │       ├── DatabaseSeeder.kt
│   │   │   │   │       ├── JsonAssetParser.kt
│   │   │   │   │       ├── dao/EmergencyDao.kt
│   │   │   │   │       └── entity/{Entities,EmergencyWithSteps}.kt
│   │   │   │   ├── di/AppModule.kt
│   │   │   │   ├── tts/TTSManager.kt
│   │   │   │   └── chat/EmergencyChatAssistant.kt
│   │   │   ├── res/
│   │   │   │   ├── values/{strings,styles}.xml
│   │   │   │   ├── drawable/ic_splash.png
│   │   │   │   ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/
│   │   │   │   └── ... (launcher icons)
│   │   │   └── assets/
│   │   │       ├── emergencies.json            # 36+ emergency scenarios
│   │   │       └── hospitals.json              # Hospital data
│   │   └── test/java/com/prathamchikitse/
│   │       └── domain/usecase/
│   │           └── GetNearestHospitalsUseCaseTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   ├── wrapper/gradle-wrapper.properties
│   └── libs.versions.toml                      # Version catalog
├── build.gradle.kts                             # Root build file
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── .gitignore
└── local.properties                             # Local SDK path (not committed)
```

---

## Screens

| Route | Screen | Purpose |
|---|---|---|
| `splash` | SplashScreen | Animated app logo + tagline (3 languages) |
| `onboarding` | LanguageOnboardingScreen | First-launch language selection |
| `home` | HomeScreen | Search bar, categories, SOS banner, emergency grid, recently viewed |
| `emergency/{id}` | EmergencyDetailScreen | Step-by-step pager with TTS controls |
| `chat/{id}` | EmergencyChatScreen | Offline first aid chatbot |
| `hospitals` | HospitalScreen | GPS-based nearby hospitals list |
| `settings` | SettingsScreen | Language, dark mode, TTS, font size |
| `sos` | SOSScreen | Pulsing dial button + emergency contacts |

---

## Dependencies

Key libraries used (managed via Gradle Version Catalog in `gradle/libs.versions.toml`):

- **Jetpack Compose BOM 2024.11.00** — Material3, Icons Extended, UI toolkit
- **Navigation Compose 2.8.5** — Screen navigation
- **Dagger Hilt 2.52** — Dependency injection
- **Room 2.6.1** — Local database
- **DataStore 1.1.1** — Preferences storage
- **Kotlinx Serialization 1.7.3** — JSON parsing
- **Play Services Location 21.3.0** — GPS for hospitals
- **Coroutines 1.9.0** — Async operations

---

## Build & Run

### Prerequisites
- Android Studio Ladybug (2024.2+) or later
- JDK 17
- Android SDK 35
- Gradle 9.0 (wrapped)

### Steps

```bash
# Clone the repository
git clone <repository-url>
cd PrathamChikitse

# Build the project
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

> **Note:** `local.properties` must contain your Android SDK path. Android Studio creates this automatically.

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use Clean Architecture layering (domain → data → UI)
- Write unit tests for use cases and ViewModels
- Ensure all existing tests pass before submitting

---

## License

Copyright © 2025 Pratham Chikitse. All rights reserved.
