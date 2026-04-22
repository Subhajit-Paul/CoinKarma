# Changelog

All notable changes to CoinKarma are documented here.  
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).  
Versions follow [Semantic Versioning](https://semver.org/).

---

## [0.1.0] — 2026-04-23

### Added
- **Home screen** — animated Aura ring (Canvas arc + pulse glow) tracking daily budget usage; colour shifts green → amber → red as the limit approaches
- **History screen** — transactions grouped by day with swipe-to-delete
- **Insights screen** — weekly bar chart (Canvas), per-category spend breakdown with progress bars
- **Quests / Challenges screen** — quest tiles with rarity badges (common / rare / epic / legendary) and per-quest progress bars; seeds four default quests on first launch
- **Profile screen** — dark-mode toggle (persisted to Room, drives the theme live), daily budget field, SMS scan toggle with pre-rationale dialog
- **Log bottom sheet** — amount input, 7-category emoji picker, optional merchant/note field
- **Bottom navigation** — Home / History / Insights / Quests / Profile tabs; Log is a sheet, not a tab
- **SMS auto-detection** — `SmsReceiver` parses Indian bank SMS (HDFC, ICICI, SBI, Axis, Kotak, PayTM, GPay formats), inserts transaction, fires a notification
- **Room database** — `TransactionEntity`, `CustomQuestEntity`, `UserProfile`; version 1 schema
- **Google Drive backup skeleton** — sign-in, `backupNow` (JSON export to `appDataFolder`), `restoreNow` + fully implemented `importJson` with `REPLACE` conflict policy
- **WorkManager nightly backup** — `BackupWorker` on `UNMETERED` network constraint, scheduled once at app start with `KEEP` policy
- **Material3 + dark theme** — Forest palette (dark default), system/manual override; design tokens from the HTML prototype

### Infrastructure
- Kotlin 2.0.21, Compose BOM 2024.09.03, Room 2.6.1, Navigation Compose 2.8.0
- KSP for Room annotation processing
- GitHub Actions CI (build + lint on every push/PR) and release workflow (APK upload on `v*` tag)

[0.1.0]: https://github.com/subhajitp/CoinKarma/releases/tag/v0.1.0
