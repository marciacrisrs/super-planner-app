# Contrato do PlanningEngine

Relacionada à #245 e à separação Plano → Programação → Rota → Execução definida na ADR 003.

## Responsabilidade

`PlanningEngine` é a fronteira do domínio que recebe um snapshot completo da realidade de planejamento e devolve uma proposta de rota.

Ele não conhece Android, Compose, Room, rede ou a origem da atividade. A origem pode ser evento, tarefa, hábito ou etapa de rotina; para o motor, tudo chega como ocorrência de atividade.

## Entrada

`PlanningInput` contém ocorrências candidatas, contexto atual, motivo do recálculo, a rota anterior quando houver e a atividade cuja execução acabou de alterar a realidade, quando aplicável. O contexto atual já concentra disponibilidade, dependências, energia, contexto, localização, deslocamentos, buffer e horário.

A entrada é tratada como snapshot: recalcular significa produzir outro resultado a partir de outro snapshot, não editar o anterior.

## Saída

`PlanningResult` contém a sequência recomendada (`route`), as atividades que não puderam ser acomodadas (`unscheduled`) e as decisões relevantes (`decisions`). Uma decisão pode ser `SCHEDULED`, `DEFERRED` ou `BLOCKED`, com motivos estruturados.

O contrato admite explicitamente trade-offs como conflito, dependência bloqueada e excesso de capacidade. O motor não precisa fingir que tudo cabe.

## Determinismo

A mesma entrada deve produzir a mesma saída. A implementação não deve depender de relógio global, banco, rede ou estado oculto.

## Wiring de produção

`DefaultPlanningEngine` é a implementação determinística de produção. Ele reutiliza as regras existentes de geração e recálculo de rota, enquanto `RecalculateRoute` permanece apenas como adaptador de compatibilidade para callers existentes.

`ObserveExecutableDay` usa o `PlanningEngine` para produzir a rota que chega à experiência de execução. Com isso, `Agora` não possui uma segunda fonte de verdade para a ordem da rota: a rota exibida nasce no motor.

## Relação com a régua #292

- **Reduz carga mental:** recebe o contexto e devolve uma proposta de rota, em vez de transferir o replanejamento para a usuária.
- **Ajuda a fazer o que cabe:** permite representar atividades que ficaram de fora.
- **Preserva prioridades:** decisões e trade-offs são dados explícitos do resultado.
- **Resolve necessidade real:** sustenta a pergunta central do produto: “o que faço agora?” quando a realidade muda.
- **Planner de papel:** preserva o modelo mental de organizar prioridades e horários; o ganho digital é recalcular sem reorganização manual.
- **Inteligência real:** define uma fronteira substituível para o motor determinístico, sem misturar UI, persistência ou IA na regra central.
- **Menos administração:** a usuária fornece a realidade; não precisa administrar a implementação do motor.

## Limite desta issue

A #245 fecha o contrato. A integração desta fatia coloca o contrato no caminho real da experiência; algoritmos específicos de seleção, posicionamento, resolução de conflitos, recorrências e recálculo continuam nas fatias próprias do motor.
