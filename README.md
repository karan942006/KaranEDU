# 🎓 KaranEDU

<p align="center">
  <img src="app/src/main/res/drawable/eduai_launcher_fg.jpg" width="120" height="120" style="border-radius: 20%;" alt="KaranEDU Logo"/>
</p>

<p align="center">
  <strong>An Offline-first personalized learning assistant designed to empower students worldwide, aligning with SDG 4 (Quality Education).</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-Stable-purple.svg?style=for-the-badge&logo=kotlin" alt="Kotlin Badge"/>
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-blue.svg?style=for-the-badge&logo=jetpackcompose" alt="Jetpack Compose Badge"/>
  <img src="https://img.shields.io/badge/Gemini_AI-Enabled-orange.svg?style=for-the-badge&logo=google" alt="Gemini AI Badge"/>
  <img src="https://img.shields.io/badge/Firebase-Auth_%26_Firestore-yellow.svg?style=for-the-badge&logo=firebase" alt="Firebase Badge"/>
  <img src="https://img.shields.io/badge/Room_Database-Offline_First-green.svg?style=for-the-badge&logo=sqlite" alt="Room DB Badge"/>
</p>

---

## 🌟 Key Features

### 🚀 1. Interactive AI Chat Tutor
* **Personalized Dialogues:** Get real-time answers to high-quality academic queries in a conversational interface powered by Google Gemini AI.
* **Offline-Ready Context:** Provides instant explanations, curriculum-aligned clarifications, and tailored guidance across different grade levels.

### 📝 2. AI Quiz Generator
* **Diagnostic Assessments:** Generate instant quizzes based on subjects, topics, or study schedules.
* **Smart Verification:** Submit answers and review performance statistics, detailed feedback, and explanations on every question.

### 📅 3. Smart Study Planner
* **Dynamic Time Slots:** Create a custom planner with focus areas and subjects (e.g., Mathematics, Physics, Essay Writing).
* **Interactive Task Tracking:** Check off tasks dynamically, keeping you highly organized and in line with your learning goals.

### 🃏 4. AI Flashcards
* **Spaced Repetition:** Convert long study topics into bite-sized, active-recall flashcards generated instantly by AI.
* **Interactive Flipping:** Interactive UI to tap, flip, and confirm recall status.

### 📈 5. Local Analytics & Student Progress
* **Rich Diagnostic Charts:** Beautifully track quiz results, study statistics, task-completion percentages, and daily milestones.
* **Activity History:** Chronological overview of all study logs.

### ☁️ 6. Cloud Sync Engine & Gmail Authentication
* **Seamless Multi-device Syncing:** Connect to **KaranEDU Cloud** using Gmail/school credentials.
* **Secure Firestore Backend:** Backup and restore user profiles, learning progress, quiz scores, and study plans to Firebase Firestore instantly.

---

## 🛠️ Architecture & Tech Stack

Following modern Android Development guidelines and **MVVM (Model-View-ViewModel)** Architecture:

* **UI Framework:** **Jetpack Compose (Material Design 3)** - Dynamic, fluid, adaptive, responsive screen sizes with rich support for light/dark themes.
* **Local Storage:** **Room Database** - Offline-first architecture caching study schedules, profiles, and historical records.
* **AI Engine:** **Google Gemini API Integration** - Connects directly using robust asynchronous calls (`Coroutines` & `StateFlow`).
* **Cloud Sync:** **Firebase Auth & Cloud Firestore** - Secure user onboarding and instant cloud backup.
* **Dependency Management:** Modern **Gradle (Kotlin DSL)** and centrally managed version catalog (`libs.versions.toml`).

---

## 📁 Repository Structure

```text
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── data/                 # Room Database, Repositories, & Firebase Sync Service
│   │   │   │   ├── Database.kt       # Entity definitions, DAOs, Database instances
│   │   │   │   └── FirebaseService.kt# Cloud Firestore & Auth logic
│   │   │   └── ui/
│   │   │       ├── theme/            # Centralized Material 3 Colors, Styles, and Themes
│   │   │       ├── screens/          # Composable screens (Login, Dashboard, Chat, etc.)
│   │   │       └── KaranEDUViewModel.kt # Main business state logic with Gemini and DB interactions
│   └── build.gradle.kts              # Application Gradle Configuration
├── metadata.json                     # AI Studio Project Metadata
├── Settings.gradle.kts               # Gradle Modules Configuration
└── .env                              # Managed Gemini AI Credentials (DO NOT commit secrets!)
```

---

## ⚙️ Quick Installation & Setup

To run KaranEDU on your local emulator or physical device, follow these steps:

### 1. Clone the project
```bash
git clone https://github.com/<your-username>/KaranEDU.git
cd KaranEDU
```

### 2. Configure Your AI Environment
KaranEDU uses the Gemini API safely and securely:
1. Obtain an API key from Google AI Studio.
2. Create or open the `.env` file in the root of your project.
3. Paste your key:
```env
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

### 3. Add Firebase Configurations (Optional for Local Mode)
If you wish to use the Firebase Sync Engine:
1. Register a project in the Firebase Console.
2. In your Android app registration, use `com.aistudio.eduai.pqtrvx` (or the applicationId defined in `app/build.gradle.kts`).
3. Download `google-services.json` and place it in the `/app` folder.

### 4. Build and Run!
Import the project into **Android Studio** and click **Run**. Gradle will automatically compile, resolve dependencies, and launch the application.

---

## 🌍 Sustainable Development Goal Alignment (SDG 4)

**KaranEDU** addresses the inequality in educational resource distribution by providing free, localized, and context-aware artificial intelligence tutoring. By offering **offline-first offline cache capabilities** via SQLite/Room database, we reduce internet accessibility barriers, bringing world-class adaptive education even to underserved and disconnected communities globally.

---

<p align="center">Made with ❤️ for quality education by the <strong>KaranEDU Team</strong></p>
