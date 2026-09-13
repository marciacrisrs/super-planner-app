# Régua permanente de produto — planner de papel

> Referência: issue #292

## Propósito

Toda feature do Super Planner deve provar que melhora a capacidade do produto de ajudar a pessoa a organizar uma vida que não cabe em uma lista de tarefas, sem transferir para ela mais trabalho de organização.

A régua usa um **bom planner de papel** como referência de simplicidade. O papel não é o limite do produto: é o teste para separar organização útil de complexidade desnecessária.

## As 7 perguntas obrigatórias

Antes de implementar uma feature, a issue deve responder:

1. **Isso reduz carga mental?**
   - Qual decisão, lembrança ou trabalho de organização deixa de ficar na cabeça da pessoa?

2. **Isso ajuda a pessoa a dar conta do que realmente consegue?**
   - A feature respeita capacidade e contexto reais, em vez de apenas adicionar coisas ao plano?

3. **Isso ajuda a não perder de vista as prioridades?**
   - Quando houver conflito ou falta de tempo, a feature protege o que importa?

4. **Isso resolve uma necessidade real ou apenas é tecnologicamente possível?**
   - Qual problema concreto justifica existir?

5. **Como uma boa versão disso funcionaria em um planner de papel?**
   - Descrever a solução manual mais simples que uma pessoa usaria.

6. **O digital/IA adiciona inteligência real ou apenas complexidade?**
   - Explicitar a vantagem que depende de software, automação, contexto ou IA.

7. **Isso exige que a pessoa administre mais o planner?**
   - Se sim, explicar por que esse custo é necessário e como será minimizado.

## Princípio de decisão

> **Se uma feature não faria sentido em um bom planner de papel, precisamos entender por quê. Se faria sentido no papel, o digital deve provar que consegue fazê-la melhor sem aumentar a complexidade.**

A régua não exige que o produto imite papel. Ela exige que qualquer complexidade adicional compre uma vantagem clara para a pessoa.

## Resultado esperado na issue

Toda nova issue de feature deve registrar, de forma curta:

- **Problema real:**
- **Como funciona no papel:**
- **Ganho digital/IA:**
- **Régua #292:** respostas às 7 perguntas.
- **Decisão:** `seguir`, `simplificar`, `adiar` ou `não implementar`.

## Relação com a execução

A régua é um gate de produto antes da implementação. Uma feature pode ser tecnicamente pequena e ainda assim ser rejeitada por aumentar carga mental ou não preservar o diferencial do Super Planner.

A implementação continua seguindo o workflow do repositório: uma issue, um branch, um PR e fechamento no merge.
