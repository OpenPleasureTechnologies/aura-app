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

Option 3: Request APK
Open an issue saying "Request APK" and I'll send you the latest build.

Requirements
Android 8.0 (API 26) or higher

Bluetooth LE capable device

Your pleasure device in pairing mode

Tested Devices
Lovense Lush 3 ✅

Lovense Max 2 ✅

We-Vibe Chorus (needs testing)

Your device? Tell us!
