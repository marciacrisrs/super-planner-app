# UX 10 — Wave 3: Capacity

## Goal

Make capacity visible as context for decision-making, not as a command or score.

The Agora experience should answer two practical questions:

1. How much planning capacity is still available today?
2. How much uninterrupted time is available before the next planned activity?

The user remains in control of what to do with that space.

## Implementation

- `AgoraUiState` exposes `capacityRemainingMinutes` and `nextWindowMinutes`.
- `AgoraViewModel` derives remaining daily capacity from the existing `DailyCapacity` model and the recalculated route.
- The next-window value is derived from the existing next activity decision rather than introducing a second scheduling engine.
- `CapacityContextCard` presents both facts together when available.
- Copy uses neutral language: capacity is described as available space, never as failure, debt, or an instruction.
- The existing low-capacity mode remains an explicit user choice and continues to use the existing `LowCapacityPlanner` policy.

## UX rules

- Never tell the user what they must do with available capacity.
- Prefer approximate language (`cerca de`) because planning durations are estimates.
- A shorter next window should be presented as context for choosing a shorter activity, not as a warning.
- Do not change domain scheduling rules in this wave.

## Acceptance criteria

- The Agora screen exposes remaining daily planning capacity when the route is available.
- When a next activity exists, the screen also exposes the time available until that activity.
- The user can still choose another activity, defer, or complete the suggestion.
- Capacity information does not mutate the agenda by itself.
- Existing low-capacity behavior remains explicit and reversible.
