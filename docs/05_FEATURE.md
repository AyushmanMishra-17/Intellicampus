# Intellicampus — Feature Trace

Use one section/file per feature when a feature is being built or significantly changed.

## FEATURE-001 — AI Tutor

**Status:** Working

### Goal
Provide students with an AI study assistant that can explain concepts, summarize material, quiz the student, and generate study plans based on the student's academic context.

### User entry points
- AI Tutor tab.
- Quick actions such as Explain, Summarize, Quiz Me, and Study Plan.
- Free-form question input.

### High-level flow
```text
Student
  |
  v
AI Tutor screen
  |
  v
Prompt + academic context
  |
  v
AI API request
  |
  v
AI response
  |
  v
Response parsing
  |
  v
Chat message displayed
```

### Acceptance criteria
- AI Tutor tab opens without crashing.
- User can submit a question.
- Request reaches the configured backend.
- Valid JSON response is parsed.
- Assistant content is displayed.
- Network/API errors produce a user-readable message.
- API credentials are not hard-coded into source files.

### Verification
- Test a simple prompt such as `Say hello`.
- Test a syllabus-related prompt.
- Test each quick action.
- Test invalid/no-network conditions.
- Verify the app remains usable after returning to another tab.

---

## FEATURE-002 — Notices

**Status:** Working

### Goal
Allow campus notices to be managed and displayed to students.

### Flow
```text
Admin management
      |
      v
Firestore notice data
      |
      v
Student notices UI
```

### Acceptance criteria
- Admin can create/update/delete notices where authorized.
- Students can view available notices.
- Empty/error states are handled.
- Data refreshes correctly.

---

## FEATURE-003 — Timetable / Schedule

**Status:** Working

### Goal
Display the student's academic timetable/schedule and provide management functionality for authorized users.

### Flow
```text
Management UI
     |
     v
Firestore timetable data
     |
     v
Schedule UI
```

### Acceptance criteria
- Timetable entries can be managed by authorized users.
- Student schedule displays correct entries.
- Empty timetable state is handled.
- Changes are reflected after refresh/reload.

---

## FEATURE TEMPLATE

### FEATURE-XXX — [Name]
**Status:** Planned / In progress / Working

### Goal
...

### User flow
...

### Files/modules involved
...

### Data involved
...

### Acceptance criteria
...

### Verification
...

### Rollback
...
