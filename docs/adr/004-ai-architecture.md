# ADR 004 — Arquitetura de IA do Super Planner

- Status: Accepted
- Date: 2026-09-13
- Parent: #242

## Contexto

O Super Planner precisa usar IA para reduzir carga mental sem transformar um LLM na autoridade sobre o planejamento. O estado do usuário permanece local e o `PlanningEngine` continua sendo a fonte de verdade sobre rota, prioridades, conflitos e capacidade.

## Decisão

Adotar o princípio:

> **Determinismo no núcleo, IA na interpretação.**

A arquitetura é:

```text
Android UI
   ↓
AiAssistant
   ↓
AiProvider
   ↓
AI Gateway remoto
   ↓
LLM Provider
```

A execução segue por comandos estruturados:

```text
LLM proposal
   ↓
Schema validation
   ↓
AiCommand
   ↓
AiToolGateway
   ↓
Use Case / Domain
   ↓
PlanningEngine
```

## Responsabilidades

### LLM / IA

- interpretar linguagem natural;
- identificar intenção;
- pedir informações faltantes;
- produzir propostas estruturadas;
- explicar decisões com evidências recebidas;
- sugerir melhorias.

### Domain / PlanningEngine

- validar estado;
- aplicar regras;
- resolver conflitos;
- calcular rota;
- persistir alterações por use cases;
- permanecer funcional sem IA.

## Gateway remoto

O aplicativo não chama o provedor de LLM diretamente. O gateway:

- mantém credenciais do provedor fora do APK;
- recebe apenas contexto mínimo;
- versiona o contrato de IA;
- aplica Structured Outputs;
- permite trocar o provedor sem alterar o domínio;
- fornece ponto único para observabilidade, limites e políticas futuras.

## Privacidade

Não enviar automaticamente a vida inteira do usuário. Cada operação deve construir apenas o contexto mínimo necessário.

Interpretação usa `store: false` no provedor remoto nesta fase.

## Fallback

Quando `AI_GATEWAY_URL` não estiver configurado ou o gateway falhar, o `HybridAiProvider` usa o `RuleBasedAiProvider` local. A indisponibilidade de IA não pode impedir o uso do Planner.

## Confirmação

Alterações materiais exigem confirmação explícita antes da execução. O LLM nunca grava diretamente em Room.

## Não decidido nesta fase

- memória longitudinal;
- agentes autônomos;
- modelo on-device;
- streaming;
- voz.

Esses recursos serão avaliados quando houver evidência de valor e contratos seguros.
