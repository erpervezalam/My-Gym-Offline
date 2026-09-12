# My Gym Offline

An offline-first Android app for gym exercises featuring **1,324 exercises** with multilingual instructions, animated GIFs, and powerful filtering capabilities — all without requiring an internet connection.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| **Exercise Library** | 1,324 exercises across 10 body parts and 17 equipment types |
| **Multilingual Support** | Instructions available in 10 languages |
| **Animated GIFs** | High-quality animated demonstrations with pinch-zoom fullscreen player |
| **Smart Filtering** | Filter by body part (grid/list) and equipment type from settings |
| **Favorites** | Heart button to save and quickly access preferred exercises |
| **Offline-First** | Zero network dependency — everything works offline |
| **Debug Tools** | Share logs via email, reset database, export telemetry |

---

## 🏗 Architecture

```
MVVM + Repository + Room Database + DataStore Preferences
```

- **UI Framework**: Jetpack Compose (Material 3)
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

---

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Room | 2.6.1 | Local database |
| DataStore | 1.1.1 | Preferences storage |
| Coil | 2.6.0 | Image/GIF loading |
| Timber | 5.0.1 | Logging |
| Navigation Compose | 2.8.3 | Navigation |
| Gson | Latest | JSON parsing |
| WorkManager | 2.9.0 | Background tasks |
| Lifecycle | 2.8.3 | ViewModel, LiveData |

---

## 📊 Dataset

This app uses the **Exercises Dataset** (1,324 exercises) which includes:

- **Exercises**: 1,324
- **Languages**: 10
- **Body Parts**: 10
- **Equipment Types**: 17
- **Thumbnails**: ~8.5 MB
- **Animated GIFs**: ~123 MB

### 🙏 Dataset Credit

The exercise data, images, and GIFs are sourced from the **[exercises-dataset](https://github.com/erpervezalam/exercises-dataset)** repository by **Er Pervez Alam**. This dataset is released under the **MIT License** (data) with media assets subject to **Gym Visual media terms**.

> **Note**: The dataset source is located at `C:\Users\mrper\Development\exercises-dataset` locally.

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or later
- JDK 17+
- Android SDK 35

### Build & Run

```bash
# Clone the repository
git clone https://github.com/erpervezalam/My-Gym-Offline.git
cd My-Gym-Offline

# Open in Android Studio and run on device/emulator
./gradlew installDebug
```

---

## 📁 Project Structure

```
My Gym Offline/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/mygymoffline/
│   │   │   ├── data/           # Room entities, DAOs, Repository
│   │   │   ├── ui/             # Compose screens, ViewModels
│   │   │   ├── util/           # Helpers, extensions
│   │   │   └── worker/         # WorkManager tasks
│   │   └── res/                # Resources (strings, themes, XML)
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml      # Version catalog
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 🛠 Development

### Key Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Lint check
./gradlew lint
```

### Debug Features

Access debug settings from the app menu:
- **Share Logs** — Email local JSON logs
- **Reset Database** — Clear all local data
- **Export Telemetry** — Export usage analytics

---

## 📄 License

This project is licensed under the **MIT License** — free for personal and commercial use.

```
MIT License

Copyright (c) 2026 Er Pervez Alam

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

### Third-Party Licenses

- **Exercise Dataset**: MIT License (data) + Gym Visual media terms — [exercises-dataset](https://github.com/erpervezalam/exercises-dataset)
- **AndroidX Libraries**: Apache License 2.0
- **Coil**: Apache License 2.0
- **Timber**: Apache License 2.0
- **Gson**: Apache License 2.0
- **Kotlin**: Apache License 2.0

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📞 Contact

**Er Pervez Alam** — [@erpervezalam](https://github.com/erpervezalam)

Project Link: [https://github.com/erpervezalam/My-Gym-Offline](https://github.com/erpervezalam/My-Gym-Offline)

---

*Built with ❤️ for fitness enthusiasts who want their workout guide everywhere — no internet required.*