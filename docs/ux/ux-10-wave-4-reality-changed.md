# UX 10 — Wave 4: Reality changed

## Goal

Make it safe and obvious to recover when the day no longer matches the plan.

The planner should acknowledge reality instead of treating a changed plan as failure.

## Principles

- A completed, skipped, delayed, or newly started activity is a change in reality, not a failure.
- Replanning is explicit: the app may recompute a suggestion, but does not silently rewrite the agenda.
- Recovery preserves fixed commitments and user priorities through the existing deterministic route rules.
- Replanning is a user-triggered refresh of the current route, not an automatic mutation.
- The user can continue with the current suggestion, choose another option, or request a fresh suggestion.

## Implementation

- `AgoraViewModel.requestReplan()` increments an internal refresh token.
- The existing route calculation is rerun with the current observed day and current time.
- `AgoraRecoveryCard` makes the recovery path visible in Agora.
- No new scheduling rule or mutation path is introduced.

## Acceptance criteria

- Agora offers a clear recovery action when the current recommendation is no longer useful.
- Replanning remains a proposal and requires explicit user confirmation before any future mutation flow.
- Copy avoids blame (`atrasada`, `falhou`, `deveria`) and focuses on the current reality.
- No automatic agenda mutation is introduced by this wave.
