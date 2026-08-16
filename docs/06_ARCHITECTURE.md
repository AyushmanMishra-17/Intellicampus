# Intellicampus — Architecture

## 1. System overview

Intellicampus consists of an Android client, Firebase services, and an external AI service.

```text
                         +----------------------+
                         |   Intellicampus App  |
                         |      Android         |
                         +----------+-----------+
                                    |
             +----------------------+----------------------+
             |                      |                      |
             v                      v                      v
     Firebase Auth            Firestore              Firebase Storage
             |                      |                      |
             +----------------------+----------------------+
                                    |
                                    v
                         Academic / user data

                                    |
                                    v
                         AI Tutor network layer
                                    |
                                    v
                    FreeLLMAPI-compatible AI service
                                    |
                                    v
                              LLM provider
```

## 2. Android client

The application uses:
- Java application code.
- Gradle Kotlin DSL (`build.gradle.kts`).
- AndroidX.
- Material Components.
- View Binding.
- Firebase SDKs.
- Fragment-based/navigation UI.

The exact class/module map should be kept synchronized with the source repository.

## 3. Firebase responsibilities

### Firebase Authentication
Responsible for user authentication/session identity.

### Firestore
Responsible for structured application data such as user/academic/campus records.

### Firebase Storage
Responsible for stored/uploaded files where used by the application.

## 4. AI service boundary

The Android app should treat the AI backend as an external service boundary.

### Client responsibilities
- Construct a valid request.
- Send it to the configured base URL.
- Authenticate using the configured key.
- Parse the expected JSON response.
- Handle failures gracefully.
- Avoid exposing credentials.

### Server/provider responsibilities
- Authenticate the request.
- Route the model request.
- Generate the completion.
- Return a structured response.

## 5. Configuration

Development configuration currently uses local Gradle properties.

Sensitive values must not be committed to Git.

Recommended separation:

```text
Source code
   |
   +--> non-secret configuration
   |
   +--> local.properties / CI secrets
           |
           +--> AI base URL
           +--> AI API key
```

## 6. Security boundary

UI visibility is not a security boundary.

Admin privileges, database access, and write permissions must ultimately be protected by Firebase/backend security rules.

## 7. Lifecycle boundary

Fragments have a view lifecycle separate from their Fragment lifecycle. Any asynchronous callback that touches views must account for that lifecycle.

This is particularly important for Firestore callbacks.

## 8. Architecture maintenance rule

Do not introduce a new architecture pattern, dependency, or networking abstraction simply because an AI assistant suggests it. Record the reason in `DECISIONS.md` and verify compatibility with the existing application.
