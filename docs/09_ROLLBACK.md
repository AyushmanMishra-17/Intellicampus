# Intellicampus — Rollback Plan

## Purpose
This document defines how to safely undo a change that causes a regression.

## Before a risky change
1. Ensure the working tree is understood.
2. Commit the currently working state.
3. Record the commit hash below.
4. Run `TEST_CHECKLIST.md`.
5. Record any known failures before starting.

## Current known-good checkpoint

**Commit:** `[INSERT CURRENT KNOWN-GOOD COMMIT HASH]`

**Version:** `1.1` / update when released

**Known-good areas:**
- Main application navigation
- Firebase-backed application flow
- Notices
- Timetable/Schedule
- Academics
- Profile
- AI Tutor
- AI backend connection

> Replace this section with the exact Git commit hash before a production/release milestone.

## Git rollback

If a new change is isolated and has not been pushed:

```bash
git status
git diff
git restore <file>
```

For a committed change that should be undone while preserving history:

```bash
git revert <commit-hash>
```

Avoid destructive commands such as `git reset --hard` unless the intended state and backup are known.

## Configuration rollback

If an AI backend/configuration change breaks the application:
1. Restore the last known-good base URL.
2. Restore the correct secret/API-key configuration locally or in CI.
3. Rebuild the app.
4. Test the API endpoint independently.
5. Run the AI Tutor checklist.

Never restore secrets by committing them to the repository.

## Firebase rollback

For Firestore/schema/data changes:
1. Identify the exact collection/document change.
2. Stop further writes if necessary.
3. Restore data from the approved backup/export if one exists.
4. Re-run application tests.
5. Verify both admin and student flows.

Do not guess at production data restoration.

## UI rollback

If branding/splash/icon changes introduce a build or rendering problem:
1. Restore the previous resource files.
2. Confirm resource names referenced by XML/Java still exist.
3. Clean/rebuild.
4. Re-run startup/navigation tests.

## After rollback
- [ ] App builds.
- [ ] App launches.
- [ ] Authentication works.
- [ ] Home works.
- [ ] Schedule works.
- [ ] Notices work.
- [ ] Academics works.
- [ ] AI Tutor works.
- [ ] Profile works.
- [ ] Regression is documented in `BUG.md`.
- [ ] `HANDOVER.md` is updated.
