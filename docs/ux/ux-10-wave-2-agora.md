# UX 10 — Wave 2: Agora

## Objective

Transform Agora from a task display into the product's primary decision moment: help the person understand what fits now, offer a coherent suggestion, explain it briefly, and preserve the person's choice.

## Implemented in this wave

- Reframed the next-action card as **a suggestion**, not an instruction.
- Changed visible action language from `Trocar próxima ação` to `Ver outra opção`.
- Changed `Começar` to `Fazer agora` to reduce the sense of a workflow command.
- Changed `Adiar` to `Deixar para depois` to make the action neutral and non-punitive.
- Rewrote recommendation explanations to avoid first-person system agency such as `Escolhi agora...`.
- Centralized feedback copy in string resources.
- Made the empty state explicitly communicate that pending work can exist without a good option fitting right now.
- Kept planning decisions and domain behavior unchanged; this wave is presentation and interaction language, not a planning-engine rewrite.

## UX contract

The Agora should answer, in order:

1. **O que posso fazer agora?**
2. **Quanto tempo isso leva?**
3. **Por que isso faz sentido agora?**
4. **Posso escolher outra coisa?**
5. **Posso deixar para depois?**

The system suggests; the person decides.

## Quality gate

A Wave 2 implementation is considered complete when the screen can communicate a useful next choice without implying that the planner has authority over the person's decision.

Next wave: **capacity / what fits** — make available capacity and constraints understandable before the recommendation is acted on.
