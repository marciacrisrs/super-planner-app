# Contrato de dados e consentimento para IA

## Princípio
A IA deve receber o menor contexto necessário para executar a solicitação. O app continua funcional sem IA.

## Dados permitidos por caso de uso

### Criar ou organizar uma atividade
Enviar somente os campos necessários para interpretar a intenção: texto fornecido pelo usuário e, quando indispensável para desambiguar, contexto temporal mínimo.

### Explicar uma decisão do Planner
Enviar apenas as evidências estruturadas retornadas pelo domínio para aquela decisão: janela disponível, prioridade, duração, prazo, conflito e compromisso adjacente quando relevante.

### Reorganizar o dia
Enviar a alteração solicitada e o subconjunto mínimo da rota afetada. Não enviar automaticamente o histórico completo do usuário.

## Dados que não devem ser enviados automaticamente
- vida inteira do usuário;
- notas sem relação com a solicitação;
- credenciais, tokens ou segredos;
- dados pessoais que não sejam necessários para a operação;
- dados de outras pessoas quando não forem necessários.

## Persistência e consentimento
- Nenhuma inferência da IA vira dado persistido sem validação apropriada.
- O usuário deve saber quando uma operação depende de serviço remoto.
- O modo sem IA deve permanecer disponível para funções determinísticas.
- Indisponibilidade de rede/provedor deve degradar para o comportamento local possível, nunca inventar sucesso.

## Logs e retenção
Logs de produção não devem registrar conteúdo pessoal completo por padrão. Eventos técnicos devem preferir IDs técnicos, tipos de operação, latência, resultado e códigos de erro.

Retenção e eventual armazenamento remoto deverão ser definidos antes da integração com um provedor específico.

## Regra de segurança do produto
A IA interpreta e sugere. O domínio valida. Os use cases persistem. O PlanningEngine calcula.
