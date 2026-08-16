# Intellicampus — Constraints

These are the boundaries an AI coding assistant must follow unless the developer explicitly overrides them.

## Security
1. Never hard-code API keys.
2. Never commit `local.properties` or secret credentials.
3. Never expose credentials in logs, screenshots, exceptions, or UI.
4. Do not weaken Firebase security rules to make a feature work.
5. Do not treat hidden UI controls as authorization.

## Scope
6. Make one logical change per request.
7. Do not rewrite unrelated working features.
8. Do not migrate frameworks or architectures without explicit approval.
9. Do not add a dependency without explaining why it is needed.
10. Do not rename large groups of files/classes unless the task requires it.

## AI Tutor
11. Preserve the configurable AI base URL.
12. Preserve the configured authentication mechanism.
13. Do not change the expected API contract without testing the deployed service.
14. Handle malformed/non-JSON responses gracefully.
15. Do not log full API keys or sensitive request content.

## Firebase
16. Do not change Firestore collections/fields blindly.
17. Verify asynchronous callbacks against the Fragment/view lifecycle.
18. Do not remove error/empty states just to hide failures.

## UI
19. Preserve the Intellicampus visual identity unless a redesign is explicitly requested.
20. Do not modify unrelated screens while working on a specific feature.
21. Prefer existing Material/AndroidX components over adding duplicate UI libraries.

## Documentation
22. Update `HANDOVER.md` after meaningful sessions.
23. Record significant architectural/technical choices in `DECISIONS.md`.
24. Record bugs in `BUG.md`.
25. Update `FEATURE.md` when feature behavior changes.
26. Update `FLOW.md` when execution/data flow changes.

## Verification
27. Never claim a change is fixed solely because code compiles.
28. Read the actual diff.
29. Run the relevant test checklist.
30. If verification was not performed, state that explicitly.
