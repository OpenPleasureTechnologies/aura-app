# 𖦹 AURA App

The mobile app that connects your pleasure device + smartwatch.

## What this repo contains

- Android app (Kotlin + Jetpack Compose)
- iOS app (SwiftUI) — coming
- Wear OS companion
- BLE connection manager

## Quick start (Android)

```bash
git clone https://github.com/OpenPleasureTechnologies/aura-app
cd aura-app/android
./gradlew assembleDebug

## Project structure

android/
├── app/
│   ├── src/main/java/org/opt/aura/
│   │   ├── MainActivity.kt
│   │   ├── bluetooth/     ← BLE scanner & connections
│   │   ├── ai/            ← TFLite inference
│   │   ├── watch/         ← Wear OS integration
│   │   └── ui/            ← Screens & components

Permissions needed

Bluetooth scan & connect

Nearby devices (Android 12+)

Notification (for foreground service)

License
MIT


## 📱 Download AURA

### Alpha Testers Wanted

**Option 1: GitHub Actions (Automatic Build)**
1. Go to Actions tab
2. Click latest workflow run
3. Download APK from Artifacts

**Option 2: Build yourself**
```bash
git clone https://github.com/OPT/aura-app
cd aura-app/android
./gradlew assembleDebug
