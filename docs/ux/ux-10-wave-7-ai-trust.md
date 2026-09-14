# UX 10 — Wave 7: AI trust and autonomy

## Goal

Make the AI boundary enforce the product promise: the Super Planner suggests; the person decides.

## Safety invariant

A proposal that requires confirmation must never mutate domain state before explicit confirmation.

The confirmation state now travels from `AiAssistant` through `AiToolGateway` to the domain use case. The gateway defaults to `confirmed = false`, preventing new callers from accidentally authorizing a mutation.

## Interaction model

```text
User intent
   ↓
AI interpretation
   ↓
Structured proposal
   ↓
Explanation + confirmation requirement
   ↓
Explicit user confirmation
   ↓
Domain mutation
```

The LLM/provider does not own canonical planner state and does not bypass the domain boundary.

## Success criteria

- unconfirmed create proposals persist nothing;
- confirmed proposals can reach the existing domain use case;
- the gateway cannot silently turn an unconfirmed call into a confirmed mutation;
- explanations remain evidence-based;
- reorganization remains a proposal until the product explicitly applies it.
