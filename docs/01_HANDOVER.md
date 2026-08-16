# Intellicampus — Handover

## Project
**Intellicampus** is an Android campus companion application built for students. The current application includes academic/campus management features and an AI Tutor.

## Current state
The project is at a working milestone. The main application flow has been tested after resolving the AI Tutor/API configuration and a HomeFragment lifecycle/null-reference issue.

### Working areas
- Authentication / user access
- Home
- Schedule / timetable
- Academics
- Notices
- Profile
- AI Tutor
- Firebase-backed data
- AI Tutor connection to the deployed FreeLLMAPI-compatible service
- Admin/management functionality for campus data, including notices and timetable-related management

> This document is a living handover. Update it after every meaningful development session.

## AI Tutor
The Android client uses a configurable base URL and API key stored locally through Gradle/local configuration rather than hard-coding credentials in source files.

The deployed service is FreeLLMAPI-compatible and exposes an OpenAI-style API. The current model response observed during testing was:
`gemma-4-31b-it`

### Important historical issue
The AI Tutor initially failed because the Android client expected a JSON object while receiving an HTML/string response from the wrong endpoint/configuration. After correcting the base URL/API configuration and request handling, the AI Tutor worked.

## Current configuration rule
- Keep secrets in `local.properties` or another local/CI secret mechanism.
- Do not commit API keys.
- Do not place API keys in Java/XML resources.
- Do not paste credentials into screenshots, README files, or Git commits.

## Known project stack
- Android / Gradle Kotlin DSL
- Java application code
- View Binding
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Material Components
- AndroidX
- FreeLLMAPI-compatible AI backend

## Before starting the next session
1. Read `ARCHITECTURE.md`.
2. Read `FLOW.md`.
3. Read the latest entries in `DECISIONS.md`.
4. Read the active `BUG.md` or `FEATURE.md`, if one exists.
5. Run the test checklist before changing working functionality.
6. Ask for a plan before implementing a non-trivial change.

## What is left
- Continue UI/branding polish.
- Keep documentation synchronized with implementation.
- Add regression tests/checks whenever a previously broken feature is changed.
- Maintain a clean release/rollback point before major changes.

## Last-session handoff
- **Completed:** AI Tutor API configuration and app-level integration troubleshooting.
- **Completed:** Major application functionality restored.
- **Completed:** Intellicampus branding assets prepared.
- **Next:** Integrate branding assets carefully without disturbing working functionality.

## Handoff format for future sessions
At the end of every session, update this section with:
1. What changed.
2. What was verified.
3. What remains.
4. Known risks.
5. Exact next step.
