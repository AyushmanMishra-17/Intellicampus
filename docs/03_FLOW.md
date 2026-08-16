# Intellicampus — Execution Flow

This document describes the high-level execution paths. Keep it updated whenever a file, service, or responsibility changes.

## 1. Application startup

```text
Android application
        |
        v
Main/navigation entry
        |
        +--> Authentication state
        |
        +--> Main application navigation
                 |
                 +--> Home
                 +--> Schedule
                 +--> Academics
                 +--> AI Tutor
                 +--> Profile
```

> Exact Activity/Fragment class names should be updated from the source tree whenever the navigation implementation changes.

## 2. Authentication/data flow

```text
User
  |
  v
Android UI
  |
  v
Firebase Authentication
  |
  v
Authenticated Firebase user
  |
  v
Firestore user/profile/academic data
```

The UI should not assume that asynchronous Firebase data has already arrived. Lifecycle-sensitive views must still exist before they are updated.

## 3. AI Tutor flow

```text
User enters question / taps quick action
              |
              v
          AI Tutor UI
              |
              v
     Build prompt/request payload
              |
              v
   Configured AI base URL + API key
              |
              v
    FreeLLMAPI-compatible backend
              |
              v
       Chat completion response
              |
              v
       Parse JSON response
              |
              v
      Extract assistant content
              |
              v
        Display AI message
```

### Failure points to check
1. Missing/incorrect base URL.
2. Missing/invalid API key.
3. Wrong endpoint path.
4. Request JSON not matching the API contract.
5. HTML/string response returned where JSON is expected.
6. Missing expected JSON fields.
7. Network failure.
8. UI updated after Fragment/View lifecycle has changed.

## 4. Firebase async UI flow

```text
UI created
   |
   v
Firebase request starts
   |
   v
Firebase returns asynchronously
   |
   +--> Verify Fragment/view is still valid
   |
   +--> Read document/result
   |
   v
Update bound views
```

Never assume an asynchronous callback will execute while the same Fragment view hierarchy still exists.

## 5. Admin management flow

```text
Admin UI
   |
   +--> Notices management
   |
   +--> Timetable management
   |
   +--> Other academic/campus management
            |
            v
        Firestore
            |
            v
       Student-facing UI
```

Keep write permissions and UI responsibilities separate. Any security-sensitive access control must be enforced by backend/Firebase security rules, not only by hiding UI controls.

## 6. Documentation flow

```text
Request
  |
  v
Plan
  |
  v
Small implementation
  |
  v
Review actual diff
  |
  v
Run TEST_CHECKLIST.md
  |
  +--> Pass --> update HANDOVER / DECISIONS
  |
  +--> Fail --> update BUG.md
                  |
                  v
               Fix + verify
```
