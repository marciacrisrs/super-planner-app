# Capacidade realista

## Decisão
O Super Planner trata a capacidade diária como um **orçamento de planejamento**, e não como a soma de todos os minutos livres do calendário.

A capacidade tem uma linha de base normal e pode receber uma declaração excepcional para um dia específico. Por padrão, apenas 80% da capacidade declarada é considerada agendável, preservando margem para transições, pausas e imprevistos.

## Por que isso existe
Uma agenda pode caber matematicamente no relógio e ainda assim falhar na prática. A régua do Super Planner privilegia o que uma pessoa realmente consegue sustentar.

## Modelo
`DailyCapacity` possui:

- `normal`: capacidade usual;
- `exceptional`: capacidade declarada para um dia fora do padrão;
- `mode`: normal ou excepcional;
- `utilizationLimit`: limite de utilização que preserva margem operacional.

A capacidade efetiva é calculável de forma determinística e possui uma operação de saldo remanescente.

## Aprendizado sem perda de controle
Cada `ActivityInstance` já registra a duração real quando concluída. Ao avaliar capacidade, uma execução concluída usa sua duração observada como evidência de esforço real, enquanto a duração planejada continua sendo controlável pela usuária para as próximas ocorrências.

Isso permite aprender com a realidade sem transformar uma medição histórica em alteração silenciosa da configuração.

## Integração com o planejamento
A capacidade fica disponível no `NextActionContext`, portanto faz parte do snapshot recebido pelo `PlanningEngine`. A seleção da próxima ação rejeita atividades que fariam o orçamento realista ser ultrapassado e registra `CAPACITY_AVAILABLE` como motivo quando a capacidade é suficiente.

O contrato do `PlanningEngine` continua puro e determinístico. Nenhuma regra depende de Android, Compose, Room ou rede.

## Régua #292

- **Reduz carga mental?** Sim: a pessoa não precisa recalcular manualmente uma margem realista.
- **Ajuda a dar conta do que cabe?** Sim: o planner não trata 100% do tempo livre como trabalho disponível.
- **Preserva prioridades?** Sim: capacidade é uma restrição; prioridade continua sendo critério de seleção.
- **Resolve necessidade real?** Sim: reduz o planejamento que só funciona no papel.
- **Funciona no papel?** Sim: um bom planner reserva margem e não preenche todos os espaços.
- **O digital adiciona inteligência real?** Sim: a aplicação consegue calcular margem e reaproveitar duração observada sem trabalho manual.
- **Aumenta a administração?** Não: o padrão funciona sem configuração adicional; a exceção é opcional e explícita.

## Fora de escopo
Aprendizado estatístico sofisticado de duração, previsão de energia e otimização global da semana permanecem como evoluções posteriores. A capacidade realista atual fornece o contrato simples necessário para essas evoluções sem antecipar complexidade.