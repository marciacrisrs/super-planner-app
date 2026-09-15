# AI confirmation boundary audit

## Rule

Any AI command that can mutate planner state must require explicit user confirmation before reaching the tool gateway.

The safety check belongs to `AiAssistant`, not only to the provider. This prevents a provider, adapter, or future gateway integration from accidentally marking a mutating proposal as safe to execute.

## Mutating commands

- `CreateActivityDraft`
- `ReorganizeDay`

These commands are always confirmation-gated.

## Non-mutating commands

- `ExplainNextActivity`
- `MissingInformation`
- `RecalculateRoute`

These may execute without confirmation because they do not mutate canonical planner state.

## Invariant

`AiAssistant.execute()` must never call `AiToolGateway.execute(..., confirmed=false)` for a mutating command.

The gateway remains responsible for forwarding the confirmation flag to the domain use case. The domain use case remains the final persistence boundary.

## Regression coverage

`AiAssistantTest` covers the adversarial case where a mutating proposal incorrectly declares `requiresConfirmation=false`. The assistant must override that declaration and wait for explicit confirmation.
