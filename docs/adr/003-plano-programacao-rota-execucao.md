# ADR 003 — Separação entre Plano, Programação, Rota e Execução

Status: aceite
Data: 2026-09-07

## Decisão

O Super Planner usa quatro conceitos distintos:

| Conceito | Responsabilidade |
|---|---|
| **Plano** | Orientação, intenção ou estrutura de origem. Diz o que deve ser feito e por quê. |
| **Programação** | Intenção temporal. Diz quando a pessoa pretende executar uma atividade. |
| **Rota** | Recomendação adaptativa. Diz o que o Super Planner recomenda executar agora e em seguida. |
| **Execução** | Registro do que realmente aconteceu. Mantém o histórico sem reescrever a orientação original. |

## Regras

1. Alterar uma Programação não altera o Plano de origem.
2. Recalcular uma Rota pode reposicionar atividades flexíveis sem alterar o Plano original.
3. A Execução preserva os intervalos planejados e realizados.
4. O motor consome ocorrências materializadas/Programações e não depende da origem concreta da atividade.
5. Eventos, tarefas, hábitos e passos de rotina podem compartilhar a mesma representação operacional de `ActivityInstance`.
6. A UI não duplica regras de geração ou seleção da Rota.

## Fluxo

```text
Plano / Evento / Tarefa / Hábito / Rotina / Recorrência
                     ↓
              Programação
                     ↓
             Motor de planejamento
                     ↓
                   Rota
                     ↓
                Execução
                     ↓
          planejado × realizado
```

## Consequências

O domínio pode evoluir novos tipos de origem sem criar uma nova regra de planejamento para cada produto. A experiência visual pode mudar sem alterar o contrato de domínio.
