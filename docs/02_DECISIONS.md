# Intellicampus — Decisions Log

This file records **why** important implementation decisions were made, not merely what changed.

## Decision 001 — Android-native application architecture
**Status:** Accepted

### Decision
Keep Intellicampus as a native Android application using the existing Android/Java/Gradle structure.

### Rationale
The project is already implemented around Android components, View Binding, Firebase, and AndroidX. Replacing the application architecture would introduce unnecessary migration risk.

---

## Decision 002 — Firebase for campus/user data
**Status:** Accepted

### Decision
Use Firebase Authentication for authentication and Firestore/Storage for application data and uploaded resources.

### Rationale
The existing application already depends on Firebase services. Keeping the existing backend avoids an unnecessary backend migration while the project is being stabilized.

---

## Decision 003 — AI Tutor through a configurable backend URL
**Status:** Accepted

### Decision
The Android app should communicate with the AI service through a configurable base URL rather than embedding a provider-specific URL throughout the application.

### Rationale
The deployed AI service can change independently from the Android client. A single configuration point makes deployment changes easier and prevents endpoint strings from being duplicated across the project.

---

## Decision 004 — Keep AI credentials out of source control
**Status:** Accepted

### Decision
Store the AI API key in local Gradle configuration such as `local.properties` during development and use secure CI/release secrets for automated builds.

### Rationale
An API key embedded in Java, XML, or a committed configuration file can be extracted from source control. Local/CI secret handling reduces accidental exposure.

---

## Decision 005 — Do not change working features while fixing an isolated issue
**Status:** Accepted

### Decision
When a feature is working, make the smallest possible change required for the next task.

### Rationale
The project has previously experienced failures caused by changes spanning multiple components. Small changes make regressions easier to identify and roll back.

---

## Decision 006 — Custom Intellicampus branding
**Status:** Accepted

### Decision
Use a consistent Intellicampus logo, launcher icon, splash branding, and custom Toast styling.

### Rationale
The application should have a recognizable visual identity across launch, in-app feedback, and system surfaces.

---

## Decision 007 — AI-assisted development requires explicit verification
**Status:** Accepted

### Decision
AI-generated code is treated as a proposal. The developer reviews the diff, understands the flow, and verifies the resulting behavior before considering the change complete.

### Rationale
The supplied AI Collaboration Field Guide emphasizes that documentation supports understanding but does not replace it.
