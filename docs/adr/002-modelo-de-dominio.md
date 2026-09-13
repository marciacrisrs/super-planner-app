# ADR 002 â€” Modelo de domÃ­nio

Status: aceite  
Issue: [#1](https://github.com/marciacrisrs/gps-da-vida-app/issues/1)  
Data: 2026-08-15

## Contexto

O **Super Planner** transforma cadastros em uma rota diÃ¡ria. Precisa distinguir o que Ã© horÃ¡rio fixo do que pode deslizar, e guardar planejado Ã— realizado. Cadastro e motor dependem deste contrato. CÃ³digo em `com.SuperPlanner.app.domain.model` â€” Kotlin puro.

## VocabulÃ¡rio

| Conceito | Papel |
|----------|--------|
| **Event** | Compromisso com inÃ­cio e fim no relÃ³gio. **Fixo.** O motor nÃ£o move. |
| **Task** | Trabalho a fazer. **FlexÃ­vel.** Tem duraÃ§Ã£o planejada, prioridade e prazo opcional. |
| **Habit** | RecorrÃªncia (ex.: dias da semana + janela). **FlexÃ­vel** dentro da janela. |
| **Routine** | SequÃªncia ordenada de passos. Ã‚ncora de horÃ¡rio opcional; passos sÃ£o flexÃ­veis em bloco. |
| **Availability** | Quando a pessoa estÃ¡ livre ou bloqueada (recorrÃªncia semanal). |
| **Priority** | Peso: obrigatÃ³rio, importante, desejÃ¡vel, lazer. ObrigatÃ³rio nÃ£o Ã© descartado sozinho. |
| **Duration** | `java.time.Duration`. Toda atividade tem duraÃ§Ã£o planejada; a realizada sÃ³ existe depois da execuÃ§Ã£o. |
| **Goal** | DireÃ§Ã£o de longo prazo. Atividades podem apontar para uma meta. Sem CRUD nesta fase. |
| **Energy** | Custo estimado (baixa / mÃ©dia / alta). O motor pode usar como preferÃªncia. |
| **Dependency** | A sÃ³ depois de B. O motor respeita quando houver dependÃªncia declarada. |
| **ActivityInstance** | OcorrÃªncia **do dia**: liga a origem (evento/tarefa/hÃ¡bito/passo) a um intervalo planejado e, depois, ao realizado e ao status. |

## Fixo vs flexÃ­vel

- **Fixo:** `Event` (e bloqueios de disponibilidade). Conflito se outro item invade o intervalo.
- **FlexÃ­vel:** `Task`, `Habit`, passos de `Routine`. Podem ser reposicionados; obrigaÃ§Ãµes seguem preservadas conforme as regras do motor.

`ActivityInstance.flexibility` deriva da origem.

## Planejado Ã— realizado

`ActivityInstance` carrega `planned` (`TimeRange`) sempre. `actual` sÃ³ apÃ³s concluir. DuraÃ§Ã£o realizada = `actual.end - actual.start` quando `actual` existe.

## RelaÃ§Ãµes

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

Disponibilidade nÃ£o gera instÃ¢ncia; o motor encaixa flexÃ­veis em janelas livres.

## Fora deste ADR

CRUD Room/UI e regras especÃ­ficas de produto. A separaÃ§Ã£o entre Plano, ProgramaÃ§Ã£o, Rota e ExecuÃ§Ã£o estÃ¡ em [ADR 003](003-plano-programacao-rota-execucao.md).

