# UX 10 — Wave 4: Reality changed

## Goal

Make it safe and obvious to recover when the day no longer matches the plan.

The planner should acknowledge reality instead of treating a changed plan as failure.

## Principles

- A completed, skipped, delayed, or newly started activity is a change in reality, not a failure.
- Replanning is explicit: the app may suggest a new route, but does not silently rewrite the agenda.
- Recovery should preserve fixed commitments and user priorities.
- Undo is available immediately after reversible changes where the existing domain action supports it.
- The user can continue with the current suggestion, choose another option, or replan.

## Acceptance criteria

- The Agora screen offers a clear recovery action when the current recommendation is no longer useful.
- Replanning remains a proposal and requires explicit user confirmation before mutation.
- The user can undo a locally applied deferral when the underlying action is reversible.
- Copy avoids blame (`atrasada`, `falhou`, `deveria`) and focuses on the current reality.
- No automatic agenda mutation is introduced by this wave.
