# Aureum (RateWatch) 🪙

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-orange.svg)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://makeapullrequest.com)
[![Stars](https://img.shields.io/github/stars/aureum-app/RateWatch?style=social)](https://github.com/aureum-app/RateWatch/stargazers)
[![Forks](https://img.shields.io/github/forks/aureum-app/RateWatch?style=social)](https://github.com/aureum-app/RateWatch/network/members)

<!-- SEO Description -->
<meta name="description" content="Aureum is a free, open-source Android app for tracking live gold rate, silver price, NIFTY 50, and SENSEX in India. Real-time precious metals rates across Indian cities with a premium glass-morphic UI.">
<meta name="keywords" content="gold price India, silver rate tracker, NIFTY 50 app, SENSEX live, Indian stock market, gold rate app download, commodity prices India, equity tracking, investment tracker, Aureum RateWatch">
<meta name="author" content="RateWatch">
<meta name="robots" content="index, follow">

<!-- Open Graph -->
<meta property="og:title" content="Aureum (RateWatch) - Gold Rate & Indian Stock Market Tracker">
<meta property="og:description" content="Open-source Android app for tracking real-time gold, silver, NIFTY 50, and SENSEX prices in India with premium UI.">
<meta property="og:type" content="software">
<meta property="og:url" content="https://github.com/aureum-app/RateWatch">

**Aureum** is a high-performance, open-source fintech application designed for the Indian market. It provides real-time tracking for **Gold**, **Silver**, and **Indian Stock Market Indices** (NIFTY 50, SENSEX) with a premium glass-morphic design.

---

## 📖 Table of Contents
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Screenshots](#-screenshots)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Getting Started](#-getting-started)
- [API Configuration](#-api-configuration)
- [Release Builds](#-release-builds)
- [Branding & Metadata](#-branding--metadata)
- [License](#-license)

---

## 🌟 Overview
Aureum (formerly RateWatch) is built to provide Indian investors with a single, beautiful interface to track their core assets. Whether you are monitoring gold prices for a wedding purchase or tracking NIFTY 50 for your portfolio, Aureum delivers real-time data with minimal latency and a state-of-the-art UI.

Optimized for the **Indian economy**, it supports multi-language localization and city-wise precious metal tracking.

---

## 🔍 Frequently Asked Questions (FAQ)

### Does Aureum support gold rate tracking for my city?
Yes! Aureum tracks 22K and 24K gold prices as well as silver rates across **all major Indian cities**. Rates are updated in real-time.

### Can I track NIFTY 50 and SENSEX on Aureum?
Yes. Aureum provides live index tracking for NIFTY 50, SENSEX, and major sectoral indices.

### Is Aureum free and open-source?
Yes. Aureum is 100% free and open-source under the MIT License. No premium paywall, no ads.

### Does Aureum work offline?
Yes. Aureum uses offline-first caching so you can access last-known prices even without an internet connection.

### What languages does Aureum support?
Aureum supports 10+ languages including Hindi, English, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, and Punjabi.

---

## ✨ Key Features
- **Live Precious Metals** — Real-time 22K & 24K Gold and Silver prices across all major Indian cities.
- **Stock Market Indices** — Dynamic tracking of NIFTY 50, SENSEX, and sectoral indices.
- **Aureum Design System** — A custom, premium UI built with glass-morphism, dark mode by default (#031427 navy), and high-fidelity animations.
- **Multilingual Support** — Available in 10+ Indic languages including Hindi, Tamil, Telugu, Bengali, Marathi, Gujarati, Kannada, Malayalam, and Punjabi.
- **Offline First** — Fast local caching for seamless performance even on slower connections.
- **Privacy Focused** — No trackers, no data collection. Just your data.

---

## 📱 Screenshots
<p align="center">
<img src="screenshots/home_overview_top.png" width="240" title="Aureum Home Screen">
<img src="screenshots/home_overview.png" width="240" title="Aureum Home Screen">
<img src="screenshots/search.png" width="240" title="Search">
<img src="screenshots/stock_details.png" width="240" title="Stock Details">
  <br>
  <em>Premium Glass-morphic UI across all core features</em>
</p>

---

## 🛠 Architecture & Tech Stack
Aureum follows modern Android development best practices and Clean Architecture:

- **Language**: [Kotlin](https://kotlinlang.org)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **Reactive Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Data Management**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or higher.
- **JDK 17** or higher.
- **Android SDK 35** (compileSdk).
- **Minimum SDK 24** (Android 7.0).

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/RateWatch.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and build the project.

---

## 📡 API Configuration
To protect sensitive endpoints and facilitate open-source contributions, Aureum uses a `local.properties` configuration system.

1. Locate `local.properties` in your root directory (automatically created by Android Studio).
2. Add your custom API base URLs:
   ```properties
   GOLD_SILVER_BASE_URL=https://your-api.com/
   STOCK_API_BASE_URL=https://your-api.com/
   ```
*Note: The app expects these endpoints to follow the [Scraper API Schema](docs/api-schema.md).*

---

## 📦 Release Builds
To generate production-ready artifacts:

### 1. Android App Bundle (.aab)
```bash
./gradlew bundleRelease
```
Path: `app/build/outputs/bundle/release/app-release.aab`

### 2. APK (.apk)
```bash
./gradlew assembleRelease
```
Path: `app/build/outputs/apk/release/app-release.apk`

---

## 🔑 Signing Configuration
The project uses `key.properties` for release signing. **Do NOT commit `key.properties` or `*.jks` files to version control.**

Example `key.properties`:
```properties
storePassword=your_password
keyPassword=your_password
keyAlias=ratewatch
storeFile=ratewatch-release.jks
```

---

## 🎨 Branding & Metadata
- **Play Store Listing**: Metadata is located in [listing.md](./playstore_listing/listing.md).
- **Assets**: High-resolution icons and branding assets are in the `playstore_listing/` directory.

---

## 📝 License
This project is licensed under the **MIT License**.

```text
MIT License

Copyright (c) 2026 RateWatch

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

Made with ❤️ for India. Track your gold, silver & equities with clarity.
