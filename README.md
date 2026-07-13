# SohojSheba (সহজ সেবা)

SohojSheba (meaning "Easy Service" in Bangla) is a modern, comprehensive digital services assistant Android application tailored for Bangladesh. It simplifies access to official government, agricultural, commercial, and personal administrative services. The app bridges the gap between complex bureaucratic procedures and the general public by using intuitive UI components, offline caching, localized multi-language support (English/Bangla), and a state-of-the-art Gemini AI assistant to generate and explain service guidelines.

---

## 🚀 Key Features

*   **Secure Authentication**: Supports Email/Password registration/login and Google One-Tap Sign-In, powered by **Firebase Authentication**.
*   **Dual-Language Localization**: Full dynamic language switching between **English** and **Bangla** across all screens, with state-aware view rendering.
*   **AI-Powered Service Generation**: Integrating Google's **Gemini AI (using `gemini-2.5-flash-lite`)** to dynamically generate specific, detailed service guidelines (instructions, required documents, fees, processing times, and helpline contacts) from raw user queries.
*   **Localized Categories**: Intelligent categorization of services:
    *   `citizen`: Personal administrative services (NID registration, passport application, birth certificate, utility connections).
    *   `farmer`: Agricultural assistance, crop/livestock subsidies, and critical land services (mutation, khatian, land deeds).
    *   `entrepreneur`: Business registration, trade licenses, VAT/TIN registration, and corporate tax guidelines.
    *   `govt_office`: Exclusively for government employees (pension applications, GPF withdrawals, service records).
*   **Fuzzy Offline Search**: Real-time fuzzy matching across localized titles and keywords to find catalog items instantly even when offline.
*   **Voice Search Support**: Built-in Android Speech-to-Text capabilities for hands-free query inputs.
*   **Offline First with Room**: SQLite caching using **Jetpack Room** to store service guidelines, user search history, and favorite bookmark lists for offline accessibility.
*   **Multi-Modal Chat Assistant**: A dedicated chat screen leveraging Generative AI for answering queries about government policies and services interactively.

---

## 🛠️ Architecture & Tech Stack

SohojSheba is built following modern Android development best practices, emphasizing modularity, testability, and responsiveness:

*   **UI Framework**: Jetpack Compose with Material Design 3 guidelines.
*   **Design Pattern**: Model-View-ViewModel (MVVM) architecture with structured UI states.
*   **Dependency Injection**: Google Dagger Hilt for clean and robust dependency resolution.
*   **Asynchronous Flow**: Kotlin Coroutines and Flows for non-blocking database queries and network calls.
*   **Local Storage**: Room Persistence Library (SQLite).
*   **Cloud Platform**: Firebase (Auth, Firestore, Cloud Functions, and Analytics).
*   **AI Engine**: Google Generative AI Android SDK (`generativeai`).

---

## 📂 Project Directory Structure

```
SohojSheba/
├── app/
│   ├── google-services.json    # Firebase configuration
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bonfire/shohojsheba/
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/        # Room Database, DAOs, and Entities
│   │   │   │   │   ├── mappers/         # Data translation models
│   │   │   │   │   ├── models/          # App data models (e.g., User)
│   │   │   │   │   ├── remote/          # Firebase Firestore API & models
│   │   │   │   │   └── repositories/    # AuthRepository, GeminiRepository
│   │   │   │   ├── navigation/          # Compose NavGraph and Route declarations
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/      # Common & decorative Compose components
│   │   │   │   │   ├── screens/         # Login, Register, Home, Chat, Details, Settings, etc.
│   │   │   │   │   └── theme/           # Material Color Schemes, Fonts, and Typography
│   │   │   │   └── utils/               # AppLocaleManager, FuzzySearch helper, etc.
│   │   │   └── res/
│   │   │       ├── values/              # Localization values (strings, colors, themes)
│   │   │       └── values-bn/           # Bangla specific strings
│   └── build.gradle.kts
├── seed/                        # Node.js catalog database seed scripts and webp images
├── gradle/                      # Version catalogs (libs.versions.toml) and wrappers
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties             # Environment configuration (API Keys)
```

---

## ⚙️ Setup & Configuration

### Prerequisites
*   Android Studio Ladybug (or newer)
*   JDK 17 or higher
*   A Firebase project with Email/Password and Google Sign-In enabled.

### Step 1: Add Firebase Configuration
1.  Download `google-services.json` from your Firebase Console.
2.  Place it inside the `app/` directory of the project.

### Step 2: Add Gemini API Key
Create a `local.properties` file in the root directory (if it doesn't already exist) and insert your Gemini API Key:
```properties
GEMINI_API_KEY=your_actual_gemini_api_key_here
```

### Step 3: Seed Database Catalog (Optional)
The project comes with a node-based catalog seeder utility under the `seed/` directory.
1. Navigate to the `seed/` directory.
2. Run `npm install`.
3. Provide your Firebase Service Account JSON as `seed/serviceAccount.json`.
4. Run the seed script to populate default categories, guides, and services to your Firestore database.

### Step 4: Build and Run
Import the project into Android Studio, let Gradle sync and download version-catalog dependencies, and run the app on a physical device or emulator running API level 26 (Android 8.0) or higher.
