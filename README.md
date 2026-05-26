# CoinKarma

A personal finance tracker for Indian users built with Kotlin + Jetpack Compose. CoinKarma auto-detects spends from bank SMS messages, visualises your daily budget as an animated "aura ring", and turns saving habits into quests with XP rewards.

---

## Screenshots

| Home | History | Insights |
|:---:|:---:|:---:|
| ![Home](docs/screenshots/home.png) | ![History](docs/screenshots/history.png) | ![Insights](docs/screenshots/insights.png) |

| Quests | Profile | Log spend |
|:---:|:---:|:---:|
| ![Quests](docs/screenshots/quests.png) | ![Profile](docs/screenshots/profile.png) | ![Log](docs/screenshots/log.png) |

> Screenshots show the Forest dark palette (default). Light mode and other palettes are available via Profile → Appearance.

---

## Features

- **Aura ring** — animated `Canvas` arc that tracks today's spend vs your daily budget; pulses and shifts colour (green → amber → red) as the limit approaches.
- **SMS auto-detection** — reads Indian bank SMS (HDFC, ICICI, SBI, Axis, Kotak, PayTM, GPay) and inserts transactions automatically; runs entirely on-device, nothing is uploaded.
- **Quest Forge** — Forge your own custom quests with custom icons, durations (15–365 days), and spend caps. Earn XP and level up your Spender Archetype.
- **Archetypes** — Choose from various spender archetypes (Zen Monk, Budget Ninja, Wealth Guardian) and personalize your profile with unique avatars.
- **Premium Animations** — Smooth, rich transitions between tabs and interactive UI elements for a fluid experience.
- **Manual log** — Bottom sheet with amount keypad, category picker, and optional merchant note.
- **History** — Transactions grouped by day with swipe-to-delete.
- **Insights** — Weekly bar chart + per-category spend breakdown.
- **Manual backup** — Exports and imports the full Room database as a local CoinKarma JSON file, no account sign-in required.
- **Multi-palette Theming** — Multiple beautiful color palettes (Forest, Cobalt, Sunset, Mint, Plum) available in both dark and light modes.

---

## Tech stack

| Layer | Library / tool |
|---|---|
| UI | Jetpack Compose, Material3 |
| Navigation | Navigation Compose 2.8 |
| State | ViewModel + StateFlow |
| Persistence | Room 2.6 (KSP) |
| Async | Kotlin Coroutines + Flow |
| Image loading | Coil 2 |
| Backup | Android document picker + JSON export/import |
| Build | Kotlin 2.0.21, AGP 8.8, Gradle 8.9 |

---

## Project structure

```
CoinKarma/
├── .github/
│   └── workflows/
│       └── release.yml       # builds release APK & AAB, creates GitHub Release on v* tag
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/coinkarma/app/
│       │   │   ├── CoinKarmaApp.kt         # Application — DB init, notification channels
│       │   │   ├── MainActivity.kt         # Single activity — reads darkMode from DB, drives theme
│       │   │   ├── nav/                    # Navigation graph and route definitions
│       │   │   ├── data/                   # Data layer (Room entities, DAOs, Database)
│       │   │   ├── platform/               # Platform services (SMS parsing, Backup, Receivers)
│       │   │   └── ui/
│       │   │       ├── atoms/              # Reusable low-level UI components (TabBar, etc.)
│       │   │       ├── screens/            # Feature screens (Home, History, Quests, Profile, etc.)
│       │   │       └── theme/              # Styling (Colors, Typography, Palettes, Icons)
│       │   └── res/
│       │       ├── values/
│       │       │   ├── colors.xml
│       │       │   ├── strings.xml
│       │       │   └── themes.xml
│       │       └── mipmap-*/
│       └── test/                     # Unit tests for business logic
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts      # Root build
├── app/build.gradle.kts  # App module build
├── gradle.properties     # Config flags
├── gradlew               # Executable wrapper
└── README.md
```

---

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 17 (Temurin / OpenJDK) |
| Android SDK | Platform 36, Build-Tools 35.0.0 |
| Gradle | 8.9 (via wrapper) |

---

## Build

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Artifacts

```bash
./gradlew assembleRelease bundleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk
# AAB: app/build/outputs/bundle/release/app-release.aab
```

To sign the release artifacts, configure `signingConfigs` in `app/build.gradle.kts` with your keystore credentials.

---

## Releases

Releases are published automatically by the [release workflow](.github/workflows/release.yml) when a `v*` tag is pushed. The workflow generates both an APK and an AAB and uploads them to the GitHub release.

---

## License

MIT — see [LICENSE](LICENSE).
