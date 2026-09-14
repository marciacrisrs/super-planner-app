# UX 10 — Wave 6: First useful day

## Goal

Reduce onboarding to the minimum context needed for the Super Planner to produce a useful first day.

The first experience should demonstrate the product value quickly:

> **O Super Planner te ajuda a decidir o que fazer agora.**

## Decisions

1. Ask for one meaningful current goal.
2. Ask for a realistic availability window.
3. Offer one optional fixed commitment as an anchor.
4. Keep every step skippable.
5. Do not ask for configuration that is not used immediately by the planning engine.
6. Do not describe the product as an AI assistant or as an authority over the user's day.
7. Finish by creating the first useful planning context, not by asking the user to configure the whole planner.

## Removed from first-run onboarding

Wake/sleep times and recurring activities are not requested during the first-run flow because this flow does not use them to produce the first useful day. They can be configured later in the appropriate product surfaces.

## Copy principles

Prefer:

- "Vamos começar pelo que importa"
- "O que você quer colocar em movimento?"
- "Quanto tempo costuma estar disponível?"
- "Tem alguma âncora importante hoje?"
- "Criar meu primeiro dia"

Avoid:

- "Configure seu planner"
- "A IA vai organizar sua vida"
- "Você precisa informar..."
- long explanations before the first interaction

## Success criteria

- A new user can reach the end of onboarding in three short steps.
- Skipping an optional step does not block progress.
- The created context is immediately persisted through existing domain repositories.
- No unused onboarding input is collected.
- The app remains neutral and autonomy-first.
