# Super Planner Design System

The theme package is the single source of truth for the Super Planner visual language.

## Foundations

- `Color.kt`: semantic palette
- `Type.kt`: typography
- `Shape.kt`: shapes and spacing
- `GpsDaVidaTheme.kt`: Material 3 theme composition
- `SuperPlannerComponents.kt`: reusable cards, actions, section headers, progress and timeline
- `SuperPlannerEmptyState.kt`: empty states and the planner icon vocabulary
- `SuperPlannerLogo.kt`: product mark

## Visual rules

- Prefer a warm cream surface, generous whitespace and large rounded containers.
- Use terracotta for the primary action and rose, sage and blue-gray as restrained accents.
- Keep hierarchy editorial: one clear focal point instead of a grid of small cards.
- Use icons as semantic anchors, not decoration everywhere.
- Empty states should explain what is missing and, when useful, offer one clear next action.
- Avoid local colors, arbitrary icon styles and Material 3 defaults that conflict with the product language.

## Component examples

```kotlin
SuperPlannerEmptyState(
    title = "Seu dia está livre",
    description = "Adicione uma atividade quando quiser planejar seu próximo passo.",
    icon = SuperPlannerIcon.CALENDAR,
    iconContentDescription = "Calendário",
    actionLabel = "Adicionar atividade",
    onAction = onAddActivity,
)
```

Screens should consume the shared theme and Super Planner primitives rather than introducing local visual constants. New visual primitives belong in this package and should include UI tests when behavior or semantics matter.
