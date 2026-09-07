# ADR 002 — Modelo de domínio

Status: aceite  
Issue: [#1](https://github.com/marciacrisrs/gps-da-vida-app/issues/1)  
Data: 2026-08-15

## Contexto

O **Super Planner** transforma cadastros em uma rota diária. Precisa distinguir o que é horário fixo do que pode deslizar, e guardar planejado × realizado. Cadastro e motor dependem deste contrato. Código em `com.gpsdavida.app.domain.model` — Kotlin puro.

## Vocabulário

| Conceito | Papel |
|----------|--------|
| **Event** | Compromisso com início e fim no relógio. **Fixo.** O motor não move. |
| **Task** | Trabalho a fazer. **Flexível.** Tem duração planejada, prioridade e prazo opcional. |
| **Habit** | Recorrência (ex.: dias da semana + janela). **Flexível** dentro da janela. |
| **Routine** | Sequência ordenada de passos. Âncora de horário opcional; passos são flexíveis em bloco. |
| **Availability** | Quando a pessoa está livre ou bloqueada (recorrência semanal). |
| **Priority** | Peso: obrigatório, importante, desejável, lazer. Obrigatório não é descartado sozinho. |
| **Duration** | `java.time.Duration`. Toda atividade tem duração planejada; a realizada só existe depois da execução. |
| **Goal** | Direção de longo prazo. Atividades podem apontar para uma meta. Sem CRUD nesta fase. |
| **Energy** | Custo estimado (baixa / média / alta). O motor pode usar como preferência. |
| **Dependency** | A só depois de B. O motor respeita quando houver dependência declarada. |
| **ActivityInstance** | Ocorrência **do dia**: liga a origem (evento/tarefa/hábito/passo) a um intervalo planejado e, depois, ao realizado e ao status. |

## Fixo vs flexível

- **Fixo:** `Event` (e bloqueios de disponibilidade). Conflito se outro item invade o intervalo.
- **Flexível:** `Task`, `Habit`, passos de `Routine`. Podem ser reposicionados; obrigações seguem preservadas conforme as regras do motor.

`ActivityInstance.flexibility` deriva da origem.

## Planejado × realizado

`ActivityInstance` carrega `planned` (`TimeRange`) sempre. `actual` só após concluir. Duração realizada = `actual.end - actual.start` quando `actual` existe.

## Relações

```mermaid
flowchart TB
  goal[Goal]
  event[Event]
  task[Task]
  habit[Habit]
  routine[Routine]
  step[RoutineStep]
  avail[Availability]
  inst[ActivityInstance]
  dep[Dependency]
  goal --> task
  goal --> habit
  routine --> step
  event --> inst
  task --> inst
  habit --> inst
  step --> inst
  dep --> inst
  avail -.-> inst
```

Disponibilidade não gera instância; o motor encaixa flexíveis em janelas livres.

## Fora deste ADR

CRUD Room/UI e regras específicas de produto. A separação entre Plano, Programação, Rota e Execução está em [ADR 003](003-plano-programacao-rota-execucao.md).
