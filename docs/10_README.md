# Intellicampus — AI Collaboration Documentation

This folder contains the project-specific documentation system based on the supplied **AI Collaboration Field Guide**.

The guide identifies five core documentation areas and a set of guardrails/review habits. In particular, it calls for:
- a living handover,
- a decisions log,
- explicit comments,
- execution flow documentation,
- bug/feature traces,
- architecture,
- constraints,
- a test checklist,
- rollback instructions,
- review of every diff,
- planning before implementation,
- small requests,
- session handoffs,
- version-pinning AI context,
- and ownership of the mental model.

The documents in this folder adapt those ideas to Intellicampus.

## Files

| File | Purpose |
|---|---|
| `01_HANDOVER.md` | Current project state and next-session context |
| `02_DECISIONS.md` | Why important technical decisions were made |
| `03_FLOW.md` | How execution/data moves through the app |
| `04_BUG.md` | Bug investigation and resolution history |
| `05_FEATURE.md` | Start-to-finish feature traces |
| `06_ARCHITECTURE.md` | System/module/service map |
| `07_CONSTRAINTS.md` | Boundaries for AI-assisted code changes |
| `08_TEST_CHECKLIST.md` | Concrete verification checklist |
| `09_ROLLBACK.md` | Safe recovery plan |
| `10_README.md` | Documentation index |

## Where to keep them

Recommended project structure:

```text
Intellicampus/
├── app/
├── gradle/
├── ...
└── docs/
    ├── 01_HANDOVER.md
    ├── 02_DECISIONS.md
    ├── 03_FLOW.md
    ├── 04_BUG.md
    ├── 05_FEATURE.md
    ├── 06_ARCHITECTURE.md
    ├── 07_CONSTRAINTS.md
    ├── 08_TEST_CHECKLIST.md
    ├── 09_ROLLBACK.md
    └── 10_README.md
```

## AI session rule

At the start of a session:
1. Read `01_HANDOVER.md`.
2. Read `06_ARCHITECTURE.md`.
3. Read `03_FLOW.md`.
4. Read relevant bug/feature documents.
5. Read `07_CONSTRAINTS.md`.

Before implementing:
1. Explain the plan.
2. Keep the request scoped to one logical change.
3. Identify affected files.
4. State how the change will be verified.

After implementing:
1. Read the actual diff.
2. Run the relevant test checklist.
3. Record significant decisions.
4. Update the handover.
5. Add/update the bug or feature trace if needed.

## Important
These documents are intentionally conservative where the source project structure was not directly inspected. Replace placeholders and verify exact class/file names against the current source tree rather than allowing documentation to invent implementation details.

The supplied Field Guide's central rule is simple: documentation supports understanding; it does not replace understanding.
