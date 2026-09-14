# UX 10 — Wave 8: Edge states

## Objective

Keep Agora useful when the day does not produce a clean next-action recommendation.

## Principles

- A missing recommendation is a valid state, not an error message.
- Completion should be understandable without requiring an activity title.
- Recovery remains explicit: the app may suggest a new route, but does not silently mutate the agenda.
- Empty and completed states must preserve the autonomy-first language from UX 10.

## Implemented behavior

### No suggestion

When no recommendation is available, Agora explains that nothing currently needs a decision and offers **Ver uma nova sugestão**. The action re-runs the existing deterministic route calculation through the ViewModel's explicit replan path.

### Completed day

When the route is complete, the card communicates completion without rendering an empty title. The fallback message is **Seu dia está em dia.**

### Future edge states

Loading, recoverable errors, and capacity-specific recovery should be represented by explicit screen states when their underlying data flow exposes those conditions. Do not introduce fake error states or silently convert infrastructure failures into planning decisions.

## Acceptance criteria

- No empty card has an unexplained dead end.
- A completed state never renders a blank activity title.
- Recovery actions are explicit and reversible.
- Existing planning/domain mutation behavior remains unchanged.
