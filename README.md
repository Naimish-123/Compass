# 🧭 Precision Compass

A modern, high-precision Android compass and spirit level application built with **Jetpack Compose** and inspired by the minimalist, pitch-black aesthetic of **Xiaomi HyperOS**.

---

## ✨ Features

### 1. 🧭 Direction Mode
* **HyperOS-Inspired Dial**: Fluid, physics-based dial rotation with radial micro-ticks and red North lubber indicator.
* **True North & Magnetic North**: Calculates magnetic declination via Android's `GeomagneticField` and GPS coordinates to deliver True North alignment.
* **Target Bearing Lock**: Tap the compass dial to lock a course heading; view real-time steering deviations (*"On Course"*, *"Turn 15° Left/Right"*).
* **Geographical Telemetry**: Displays current coordinates (Degree-Minute-Second format) and altitude in meters.
* **Subtle Tactile Feedback**: Distinct haptic signatures when crossing primary cardinal points (N, E, S, W).

### 2. ⚖️ Spirit Level Mode
* **Dual-Surface Sensing**:
  * **Surface / Flat Mode**: Concentric bullseye target with floating spirit bubble. Merges into a solid accent ring when reaching exact 0° alignment.
  * **Vertical / Wall Mode**: Artificial horizon line that tilts dynamically for hanging wall art, shelves, and vertical leveling.
* **0° Alignment Snap**: Haptic feedback triggers when achieving flat alignment.

### 3. ⚙️ Hardware & Diagnostics
* **Sensor Fusion Engine**: Prioritizes fused 9-axis `TYPE_ROTATION_VECTOR`, falling back to low-pass filtered accelerometer and magnetometer data.
* **Interactive Calibration Guide**: Animated figure-8 (Bernoulli lemniscate) visual guide to calibrate device magnetometer interference.
* **Simulation Demo Mode**: Built-in simulator for smooth orientation testing on Android emulators or devices without physical sensors.
* **100% Offline & Private**: Zero network permissions required. All calculations run strictly on-device.

---

## 🛠️ Tech Stack & Architecture

* **UI**: 100% Jetpack Compose with Material 3
* **Language**: Kotlin 2.2
* **Architecture**: MVVM with unidirectional data flow (StateFlow)
* **Concurrency**: Kotlin Coroutines
* **Sensors**: Android `SensorManager`, `LocationManager`, `GeomagneticField`
* **Haptics**: Android 12+ `VibratorManager` composition primitives with legacy fallbacks
* **Testing**: Robolectric + JUnit 4

---

## 🚀 Building & Running

### Prerequisites
* **Java**: JDK 21
* **Android SDK**: Compile SDK 36 (Android 16), Min SDK 24 (Android 7.0)

### Local Build
Clone the repository and build the debug APK using Gradle:

```bash
# Clone the repository
git clone https://github.com/<your-username>/Compass.git
cd Compass

# Build Debug APK
./gradlew assembleDebug
```

The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Running Tests
Execute the local Robolectric unit test suite:

```bash
./gradlew test
```

---

## 📦 Automated GitHub Actions CI

This repository includes a continuous integration workflow at [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml) that automatically builds the Android APK on every push or pull request to `main`.

### Downloading the APK:
1. Go to the **Actions** tab on GitHub.
2. Select the latest workflow run.
3. Scroll down to **Artifacts** and download **`precision-compass-debug`**.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
