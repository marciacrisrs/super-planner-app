# ADR 001 â€” Arquitetura inicial

Status: aceite  
Issue: [#17](https://github.com/marciacrisrs/gps-da-vida-app/issues/17)  
Data: 2026-08-15

## Contexto

GPS da Vida monta uma rota diÃ¡ria a partir de eventos, tarefas, hÃ¡bitos e rotinas, e responde â€œo que faÃ§o agora?â€. O valor estÃ¡ no motor local e nas telas Agora / Meu Dia. Sync, conta e IA nÃ£o entram nesta fase.

## DecisÃµes

| Tema | Escolha |
|------|---------|
| Plataforma | Um app Android, offline-first, sem backend |
| UI | Jetpack Compose, Material 3 |
| Linguagem | Kotlin; identificadores e pacotes em inglÃªs; copy da UI em portuguÃªs |
| MÃ³dulos | Um Gradle module `:app` com pacotes `domain` / `data` / `ui` |
| minSdk / target | 26 / 36 (ajustar target na #18 se o template AGP divergir) |
| Namespace / applicationId | `com.SuperPlanner.app` |
| DI | Hilt |
| PersistÃªncia | Room (Ãºnica fonte local) |
| NavegaÃ§Ã£o | Navigation Compose |
| Tempo | `java.time.Clock` injetado (testes de atraso/reagendamento) |

Multi-mÃ³dulo sÃ³ depois de ADR novo, se o Gradle doer.

## Camadas

```text
app/src/main/java/com/SuperPlanner/app/
  domain/          Kotlin puro (sem Android)
    model/
    planning/      motor isolado (agenda, â€œagoraâ€, recÃ¡lculo)
    usecase/
    port/          interfaces que data implementa
  data/            Room, mappers, repositÃ³rios
  ui/              Compose, ViewModels finos, navigation
  di/              mÃ³dulos Hilt
```

DependÃªncias: `ui` â†’ `domain` â† `data`. `ui` nÃ£o importa Room. `domain` nÃ£o importa Android, Compose nem Room.

```mermaid
flowchart LR
  ui[ui Compose]
  domain[domain]
  data[data Room]
  ui --> domain
  data --> domain
```

## PersistÃªncia local

- Room guarda o que o usuÃ¡rio cadastrou e o estado do dia (conclusÃ£o, duraÃ§Ã£o real).
- Entidades Room ficam em `data`. Mappers convertem para modelos de `domain`.
- DAOs nÃ£o vazam para Compose nem para o motor.
- Sem DataStore nesta fase, salvo preferÃªncia pontual se a #18 precisar (ex.: tema). Sem rede.

## NavegaÃ§Ã£o

Grafo mÃ­nimo (destinos crescem nas issues de tela/CRUD):

- `agora` â€” tela principal
- `meu_dia` â€” timeline do dia
- grafo de cadastro depois (#2â€“#8): eventos, tarefas, hÃ¡bitos, rotinas, disponibilidade

Uma `Activity` (`MainActivity`) + `NavHost`. ViewModels com escopo de destino/hilt.

## Contratos domÃ­nio â†” UI

A UI sÃ³ fala com o domÃ­nio via use cases. Entrada: intenÃ§Ã£o do usuÃ¡rio. SaÃ­da: modelos imutÃ¡veis / estado jÃ¡ calculado.

Contratos iniciais (nomes podem fechar na #1):

| Use case | Para a UI |
|----------|-----------|
| `GetNowRoute` | atividade atual, prÃ³xima, depois, relÃ³gio |
| `GetDayAgenda` | timeline ordenada + conflitos |
| `CompleteActivity` / `SkipActivity` / `DeferActivity` | novo estado persistido; motor pode recÃ¡lcular |
| `RecalculateRoute` | disparado por atraso, novo item, cancelamento â€” nÃ£o pela UI â€œna mÃ£oâ€ |

Regras de prioridade, duraÃ§Ã£o, horÃ¡rios fixos vs flexÃ­veis e reagendamento vivem em `domain/planning`. ViewModel: chama use case, expÃµe `UiState`, zero recÃ¡lculo.

RelÃ³gio: Hilt fornece `Clock.systemDefaultZone()` em produÃ§Ã£o; testes passam `Clock.fixed(...)`.

## EstratÃ©gia de testes

1. JVM em `src/test`: motor, use cases, `Clock` fake. PreferÃªncia absoluta para #9â€“#12 e #16.
2. Testes de mapper/repositÃ³rio com Room in-memory quando houver #18.
3. Compose / instrumentado sÃ³ nos caminhos crÃ­ticos (concluir em um toque na Agora).
4. Nome do teste = critÃ©rio de aceite que ele trava.
5. Qualidade (Lint, Detekt, CI) no fim do lote, skill `android-quality`, depois que #18 existir.

## EvoluÃ§Ã£o para IA

O motor em `domain/planning` Ã© a Ãºnica porta de â€œqual Ã© a prÃ³xima aÃ§Ã£o / nova rotaâ€. Uma IA futura (on-device ou remota) entra como outra implementaÃ§Ã£o atrÃ¡s da mesma porta (`PlanningEngine`), sem reescrever Compose nem Room. Nesta fase a implementaÃ§Ã£o Ã© determinÃ­stica (regras). Sem SDK de LLM no app agora.

## Fora de escopo

Backend, login, sync, widgets, Wear, XML Views, WorkManager (salvo necessidade explÃ­cita depois).

## PrÃ³ximo

Issue **#18**: esqueleto Gradle/Compose/Hilt/Room/Navigation que respeita este ADR. Depois **#1** (modelo de domÃ­nio).

