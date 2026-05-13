# RateWatch

**RateWatch** is a modern Android app to monitor live **Gold**, **Silver**, and **Stock** prices in India.

- Real-time gold & silver prices across major Indian cities (powered by Goodreturns.in)
- Live Indian stock indices (Nifty 50, Sensex, Bank Nifty) and popular stocks
- Full support for **10 Indic languages** + English (onboarding + runtime language switching)
- Beautiful Material 3 UI with dark mode
- Watchlist for tracking your favorite assets

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog | 2023.1.1+ (or newer)
- JDK 17+
- Android SDK 35 (compileSdk)
- Minimum SDK 24 (Android 7.0)

### Build & Run from Terminal

```bash
# 1. Clone the repository (if you haven't already)
git clone <your-repo-url>
cd RateWatch

# 2. Make gradlew executable (first time only)
chmod +x gradlew

# 3. Build the debug APK
./gradlew assembleDebug

# 4. Build the release bundle (.aab)
./gradlew bundleRelease

# 5. Build the release APK
./gradlew assembleRelease

# 6. Install and launch on a connected device or emulator
./gradlew installDebug
```

### Run from Android Studio

1. Open the project in Android Studio
2. Sync Gradle files
3. Select a device/emulator
4. Click **Run** (or press `Shift + F10`)

The generated debug APK will be available at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## ✨ Features

- **Gold & Silver Prices** — Fetched live from `https://api.example.com/scraper/metals/latest`
  - 22K & 24K Gold (per 10g)
  - Silver (per kg)
  - City-wise prices (Delhi, Mumbai, Chennai, Bangalore, Hyderabad, etc.)
- **Stocks & Indices** — Nifty 50, Sensex, Bank Nifty + major stocks via Yahoo Finance
- **Watchlist** — Save and track your favorite metals and stocks
- **Multi-language Support** — Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, Punjabi + English
- **Modern UI** — Material 3, dynamic theming, smooth animations

---

## 🛠 Tech Stack

- **Kotlin** + **Jetpack Compose** + **Material 3**
- **Hilt** (Dependency Injection)
- **Retrofit** + **OkHttp** (Networking)
- **DataStore** (Preferences & persistence)
- **Kotlin Coroutines** + **Flow**
- **Navigation Compose**

---

## 📡 APIs Used

| Data              | Source                                      |
|-------------------|---------------------------------------------|
| Gold & Silver     | `https://api.example.com/scraper/metals/latest` (Goodreturns.in) |
| Stocks & Indices  | Yahoo Finance (`query1.finance.yahoo.com`)  |

> Base URLs are configured in `app/build.gradle.kts` via `buildConfigField`.

---

## 📝 License

```
MIT License

Copyright (c) 2026 RateWatch
```

---

## 📦 Release Builds

To generate production-ready artifacts, use the following commands:

### 1. Generate Android App Bundle (.aab)
Recommended for Play Store submission.
```bash
./gradlew bundleRelease
```
The AAB will be available at:
`app/build/outputs/bundle/release/app-release.aab`

### 2. Generate APK (.apk)
Useful for manual distribution or testing.
```bash
./gradlew assembleRelease
```
The APK will be available at:
`app/build/outputs/apk/release/app-release.apk`

---

## 🔑 Signing Configuration

The project uses `key.properties` (in the root directory) to manage release signing. 

**Wait, do NOT commit `key.properties` or `*.jks` files to version control.**

If you need to generate a new keystore:
```bash
keytool -genkeypair -v -keystore ratewatch-release.jks -alias ratewatch -keyalg RSA -keysize 2048 -validity 10000
```

Ensure your `key.properties` matches the generated keystore:
```properties
storePassword=your_password
keyPassword=your_password
keyAlias=ratewatch
storeFile=ratewatch-release.jks
```

---

## 🎨 Branding & Metadata

- **Play Store Listing**: Find optimized titles and descriptions in [listing.md](./playstore_listing/listing.md).
- **App Icon**: Premium Material 3 icons are located in the `playstore_listing/` directory.

---

Made with ❤️ for India. Monitor your gold, silver, and stocks — in your language.
