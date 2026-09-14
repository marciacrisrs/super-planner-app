# Beta experiment — #260

## Goal

Validate whether Super Planner works for people other than its creator before expanding scope.

The experiment must produce evidence for one of three decisions:

1. **Continue** — the core promise is understood, trusted and useful.
2. **Change product** — the product is useful but the audience, workflow or value proposition needs adjustment.
3. **Stop/pause** — the product does not solve a sufficiently important problem in observed use.

## Sample

Recruit **5–10 people** with intentionally different planning profiles. Record only a study participant code; do not store unnecessary personal data in the repository.

Suggested diversity:

- one person with highly structured planning;
- one person who normally does not use a planner;
- one person with fragmented availability;
- one person with many fixed commitments;
- one person who frequently replans during the day.

## Session protocol

### 1. Onboarding

Ask the participant to explain, in their own words, what they think the app does.

Measure:

- time to first meaningful action;
- questions/confusion without prompting;
- whether the participant understands that the planner proposes a route rather than controlling their life.

### 2. First planning

Give the participant a realistic week and ask them to enter their own reality.

Observe:

- what they expect to enter first;
- whether fixed commitments are understood;
- whether activities, priorities and availability make sense;
- where the data model conflicts with their mental model.

### 3. Agora

Ask the participant to use **Agora / O que faço agora?** during a realistic moment.

Measure:

- whether the recommendation is understandable;
- whether it fits the available time;
- whether the participant acts on it;
- confidence before acting (1–5);
- confidence after acting (1–5).

### 4. Replanning

Introduce one controlled change: delay, cancellation or a new commitment.

Observe:

- whether the recalculation helps;
- whether alternatives are understandable;
- whether the participant fears losing the original plan;
- whether explicit confirmation feels safe or cumbersome.

### 5. Value / willingness to pay

Ask only after observed use:

> “Se este produto resolvesse esse problema de forma confiável, você pagaria por ele?”

Capture the answer and the reason, without leading the participant.

## Evidence sheet

For every participant record:

| Dimension | Measure |
|---|---|
| Product understanding | Can explain value proposition without help: yes/no |
| First planning | Completed independently: yes/no |
| Trust | Confidence before Agora: 1–5 |
| Trust after use | Confidence after Agora: 1–5 |
| Execution | Used recommendation: yes/no |
| Replanning | Felt helped / neutral / harmed |
| Confusion | Count + exact observed moment |
| Motor errors | Count + scenario |
| Willingness to pay | yes / no / conditional + reason |

Do not treat a single participant comment as a product truth. Separate **observation**, **interpretation**, and **decision**.

## Success signals

Use the following as decision aids, not as a substitute for judgment:

- at least 4/5 participants can explain the core value after onboarding without help;
- at least 4/5 complete first planning without facilitator intervention for the core workflow;
- at least 4/5 use Agora at least once in a realistic task;
- median confidence after Agora is >= 4/5;
- no participant experiences a silent destructive change to fixed commitments;
- recurring motor failures are reproducible and become engineering issues;
- at least 3 participants describe a concrete problem the product solves and would miss if removed;
- willingness to pay is supported by observed value, not only hypothetical enthusiasm.

## Stop / pivot signals

Escalate a product decision if:

- most participants cannot explain why the app exists;
- entering reality feels like more work than using a paper planner;
- participants systematically ignore Agora recommendations;
- participants do not trust recalculation even when it is correct;
- the same conceptual misunderstanding appears across multiple profiles;
- the perceived value is limited to a generic task list/calendar.

## Analysis

After the sessions, create a matrix by participant and classify every finding as:

- `OBSERVATION`
- `CONFUSION`
- `MOTOR_ERROR`
- `TRUST`
- `VALUE`
- `FEATURE_REQUEST`

Then aggregate recurring findings. A feature request is not automatically a priority; prioritize only findings that block understanding, trust, execution or willingness to pay.

## Decision record

The experiment is **not complete** until there is a dated decision record answering:

1. Who is the product for now?
2. What painful problem did the evidence validate?
3. Which behavior proves the product is useful?
4. Which motor/product failures must be fixed before more users?
5. What should be removed or simplified?
6. Continue, change, or stop — and why?

Real-user evidence must be collected outside the repository; this document deliberately does not fabricate that evidence.
