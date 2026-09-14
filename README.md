# Super Planner

App Android offline-first para planejar a vida e transformar Programações em uma Rota adaptativa — com foco em “o que faço agora?”.

- Arquitetura: [ADR 001](docs/adr/001-arquitetura-inicial.md)
- Domínio: [ADR 002](docs/adr/002-modelo-de-dominio.md)
- Plano → Programação → Rota → Execução: [ADR 003](docs/adr/003-plano-programacao-rota-execucao.md)
- Produto: [Régua permanente de produto](docs/product/002-regua-permanente-planner-de-papel.md)
- Entrega: [WORKFLOW](docs/WORKFLOW.md)

## AI Gateway

O app pode usar o `Super Planner AI Gateway` como provedor remoto de interpretação. O domínio continua responsável por validar e aplicar qualquer proposta; o Gateway não acessa Room nem executa o PlanningEngine.

Configure a URL sem credenciais no build:

```bash
./gradlew assembleDebug -PAI_GATEWAY_URL=https://<gateway-host>
```

O cliente remoto usa `POST /v1/ai/propose` com o contrato versionado `schemaVersion = 1`. Se o gateway não estiver configurado, ou se a falha for de transporte, o `HybridAiProvider` mantém o interpretador local como fallback.

Abrir a pasta no Android Studio, sincronizar o Gradle e rodar no emulador. O package Android é `com.superplanner.app`.
