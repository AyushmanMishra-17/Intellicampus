# Intellicampus — Test Checklist

This is the project's proof checklist. A feature is not considered complete because an AI assistant says it is complete; the actual application behavior must be verified.

## A. Build verification
- [ ] Gradle sync succeeds.
- [ ] Debug build succeeds.
- [ ] No new compile errors.
- [ ] No unexpected dependency resolution failures.
- [ ] No accidental secret files are included in the commit.

## B. Startup
- [ ] App launches.
- [ ] Splash/branding displays correctly.
- [ ] No immediate crash.
- [ ] Authentication state resolves correctly.

## C. Authentication
- [ ] Login works.
- [ ] Logout works.
- [ ] Invalid credentials show an appropriate error.
- [ ] User session persists/reloads correctly.

## D. Home
- [ ] Home opens.
- [ ] User name/profile information loads.
- [ ] No crash while Firestore callbacks complete.
- [ ] Empty/missing user data does not crash the UI.

## E. Schedule / timetable
- [ ] Schedule tab opens.
- [ ] Existing entries display.
- [ ] Empty state displays correctly.
- [ ] Management changes appear after refresh/reload.
- [ ] Invalid/missing entries are handled safely.

## F. Notices
- [ ] Notices display.
- [ ] Empty state works.
- [ ] Notice management works for authorized users.
- [ ] Create/update/delete operations behave correctly.
- [ ] Students cannot perform unauthorized management operations.

## G. Academics
- [ ] Academics screen opens.
- [ ] Semester/batch information is correct.
- [ ] Academic data loads from the expected source.
- [ ] Missing data does not crash the app.

## H. AI Tutor
- [ ] AI Tutor tab opens.
- [ ] No Fragment/View lifecycle crash.
- [ ] Free-form prompt sends successfully.
- [ ] Simple test: `Say hello`.
- [ ] Explain action works.
- [ ] Summarize action works.
- [ ] Quiz Me action works.
- [ ] Study Plan action works.
- [ ] Syllabus-aware request works.
- [ ] API response is parsed as JSON.
- [ ] Assistant text appears.
- [ ] Invalid API key produces a controlled error.
- [ ] Network failure produces a controlled error.
- [ ] HTML/non-JSON response produces a controlled error.
- [ ] API key is not visible in logs/UI.

## I. Profile
- [ ] Profile opens.
- [ ] User information loads.
- [ ] Editable fields behave correctly.
- [ ] Logout/account actions work.

## J. Navigation regression
- [ ] Home -> Schedule
- [ ] Schedule -> Academics
- [ ] Academics -> AI Tutor
- [ ] AI Tutor -> Profile
- [ ] Profile -> Home
- [ ] Repeat navigation several times.
- [ ] Rotate/recreate activity if supported by the app.
- [ ] Return from background and verify no crash.

## K. Release sanity
- [ ] Version code/name are intentional.
- [ ] Release build succeeds.
- [ ] No secrets are packaged unintentionally.
- [ ] AI backend URL is configured correctly for the target environment.
- [ ] Firebase configuration is correct.
- [ ] App icon/splash assets are present.

## Test record

### Date
YYYY-MM-DD

### Build/version
...

### Tester
...

### Result
PASS / FAIL

### Failures
...

### Follow-up bug
...
