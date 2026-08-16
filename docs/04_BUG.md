# Intellicampus — Bug / Incident Log

Use this file for bugs that require investigation. Keep completed incidents instead of deleting them; they provide regression history.

## BUG-001 — AI Tutor returned non-JSON/HTML response

**Status:** Resolved

### Symptom
The AI Tutor displayed an error similar to:

`Value <!doctype of type java.lang.String cannot be converted to JSONObject`

### Context
The Android client was expecting a JSON response but received content that began like an HTML document/string.

### Investigation
The deployed AI service and Android client's configured endpoint/base URL were checked. The API endpoint was then tested independently to confirm the actual response format.

A subsequent API test returned a structured chat-completion response containing:
- `object: chat.completion`
- a generated completion ID
- model information
- choices
- usage data

### Root cause
The Android client was not consistently communicating with the intended API endpoint/configuration.

### Resolution
The base URL/API configuration was corrected and the app was updated to use the working configuration.

### Verification
- API endpoint responded with structured JSON.
- AI Tutor request completed successfully.
- AI Tutor UI displayed the returned answer.

### Regression checks
Run the AI Tutor section of `TEST_CHECKLIST.md` after any API/client networking change.

---

## BUG-002 — HomeFragment asynchronous callback NullPointerException

**Status:** Resolved

### Symptom
The application crashed on the main thread with a `NullPointerException` while trying to access:

`LayoutHomeHeaderBinding.tvUserName`

from a Firestore success callback in `HomeFragment`.

### Key lesson
The callback executed asynchronously and attempted to access a view binding that was no longer valid.

### Resolution
The HomeFragment view/binding lifecycle handling was corrected so asynchronous callbacks do not blindly update destroyed/null views.

### Verification
- App no longer crashes when opening the AI Tutor tab/navigation path.
- Main navigation and Home behavior were re-tested after the fix.

### Regression checks
Run startup, Home, navigation, and profile/user-data tests after changing Fragment lifecycle code.

---

## BUG TEMPLATE

### BUG-XXX — [Short title]
**Status:** Open / In progress / Resolved

### Symptom
What the user sees.

### Reproduction
1. ...
2. ...
3. ...

### Expected
...

### Actual
...

### Investigation
...

### Root cause
...

### Fix
...

### Verification
...

### Regression checks
...
