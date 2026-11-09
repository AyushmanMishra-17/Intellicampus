# IntelliCampus 🎓

### Your Smart Academic Companion

**IntelliCampus** is an AI-powered Android application designed to simplify academic management, enhance student collaboration and make learning more accessible.

The platform brings together essential academic tools, AI-powered learning assistance, student community features and personal productivity tools in one unified application.

Built with Java, XML and Firebase, IntelliCampus aims to create a smarter and more connected academic experience for students, teachers and administrators.

---

## ✨ Features

### 🤖 AI Tutor
An integrated AI-powered learning assistant that helps students with their academic needs.

- Ask academic questions and receive AI-generated explanations.
- Generate summaries and study notes.
- Create practice quizzes.
- Generate personalized study plans.
- Get syllabus-aware academic assistance.
- Access persistent AI chat history.
- Export AI-generated notes directly as PDF.
- Configure individual API keys and AI providers.
- Support for FreeLLMAPI and OpenAI-compatible APIs.
- Custom API endpoint and model configuration.

### 📚 Academic Management
A centralized platform for managing essential academic information.

- Student profiles and academic information.
- Academic batch management.
- Semester management and promotion.
- Timetable management.
- College notices and announcements.
- Assignment management.
- Curriculum and syllabus information.

### 👥 Student Community
A dedicated space for academic discussions and student interaction.

- Create and participate in discussions.
- Ask questions and share academic ideas.
- Comment on discussion posts.
- Interact with students across different academic batches.
- Discover classmates through the student directory.
- View relevant academic profile information.

### 📅 Study Planner
A personal productivity tool to help students stay organized.

- Create and manage study tasks.
- Add descriptions and deadlines.
- Mark tasks as completed.
- Track pending and completed tasks.
- Maintain a structured study schedule.

### 🛡️ Administration and Access Management
Role-based functionality for managing academic operations.

- Student, teacher and administrator roles.
- Secure authentication.
- Administrative batch management.
- Notice management.
- Timetable management.
- Assignment management.
- Firebase-backed academic data management.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| Platform | Android |
| Language | Java |
| UI Development | XML |
| Design System | Material Design / Material Components |
| IDE | Android Studio |
| Build System | Gradle Kotlin DSL |
| Authentication | Firebase Authentication |
| Database | Cloud Firestore |
| File Storage | Firebase Storage |
| AI Integration | FreeLLMAPI-compatible APIs |
| AI Backend Hosting | Render |
| Version Control | Git and GitHub |

---

## 🏗️ Architecture

IntelliCampus follows a modular Android application architecture, separating UI components, data models, repositories, services and application logic.

The application integrates Firebase services for authentication and cloud data management, while the AI Tutor communicates with configurable AI services through its backend integration.

The architecture is designed to preserve existing functionality while allowing individual modules to evolve independently.

---

## 🔐 Security and Privacy

Security and access control are important considerations in IntelliCampus.

- Firebase Authentication for user authentication.
- Firestore security rules for role-based permissions.
- Student-specific access to personal study tasks.
- Batch-restricted student directory access.
- Cross-batch discussion forum access.
- Encrypted local storage for individual AI API credentials using Android Keystore-backed encryption.
- No hardcoded API credentials in application source code.

---

## 📱 Installation

To install IntelliCampus:

1. Visit the [GitHub Releases](https://github.com/AyushmanMishra-17/Intellicampus/releases) page.
2. Download the latest signed APK.
3. Open the APK on your Android device.
4. Allow installation from the requested source if prompted.
5. Complete the installation.
6. Register or log in to access the application.

**Requirements:**
- An Android device compatible with the application's configured minimum SDK.
- An active internet connection for Firebase-backed features.
- Valid AI provider credentials for AI Tutor functionality.

---

## 📖 Project Documentation

IntelliCampus includes a dedicated documentation system for maintaining technical knowledge and supporting future development.

The documentation is available in the `docs/` directory.

| Document | Description |
|---|---|
| `01_HANDOVER.md` | Current project status and development handover |
| `02_DECISIONS.md` | Technical decisions and their reasoning |
| `03_FLOW.md` | Application execution and data flows |
| `04_BUG.md` | Bug investigation and resolution history |
| `05_FEATURE.md` | Feature implementation records |
| `06_ARCHITECTURE.md` | Application architecture |
| `07_CONSTRAINTS.md` | Development guidelines and restrictions |
| `08_TEST_CHECKLIST.md` | Testing and verification checklist |
| `09_ROLLBACK.md` | Recovery and rollback procedures |
| `10_DOCUMENTATION_GUIDE.md` | AI collaboration and documentation guidelines |

---

## 🚀 Project Status

<kbd>Feature-complete for the current development phase</kbd>

The current IntelliCampus release brings together academic management, AI-powered learning, student collaboration and personal productivity features.

The project is considered complete for its current scope, with future maintenance, improvements and additional functionality remaining possible.

---

## 🔮 Future Scope

Potential areas for future development include:

- Expanded AI provider integrations.
- Additional academic productivity tools.
- Enhanced student collaboration.
- Improved analytics and reporting.
- Further UI and accessibility improvements.

---
## 📄 License

Add the appropriate license file and license information here before distributing the project under an open-source license.

---

<p align="center">

**IntelliCampus**

*Learn Smarter. Stay Organized. Stay Connected.*

Built with ❤️

</p>