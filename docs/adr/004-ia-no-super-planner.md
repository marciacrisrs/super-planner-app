# ADR 004 — IA no Super Planner

- **Status:** Accepted
- **Date:** 2026-09-13

## Contexto
O produto precisa aceitar linguagem natural, ajudar a organizar intenções, explicar decisões e sugerir reorganizações. O LLM não pode se tornar a autoridade sobre estado ou regras do Planner.

## Decisão
Adotar a arquitetura:

`UI → AI Assistant → ferramentas/use cases → Domain → PlanningEngine`

O `PlanningEngine` permanece determinístico, testável e independente de Android, Compose, Room e provedor de IA. O assistente de IA produz comandos estruturados e explicações apoiadas em fatos do domínio.

## Limites
A IA não acessa banco diretamente, não altera a rota diretamente, não inventa dados, não decide conflitos por conta própria e não sobrescreve mudanças materiais sem confirmação quando aplicável.

## Seleção de provedor
O produto terá uma interface de provedor de IA. A implementação concreta pode ser remota ou on-device sem alterar o domínio.

## Privacidade
O contexto enviado deve ser minimizado segundo `docs/privacy/001-ia-dados-consentimento.md`. Ausência de IA ou indisponibilidade do provedor não pode corromper o estado local.

## Consequências
Ganhamos substituibilidade de modelos, determinismo do núcleo, testabilidade e controle de privacidade. Em troca, a camada de IA precisa respeitar schemas e contratos de ferramentas e não pode resolver problemas do domínio por meio de texto livre.
