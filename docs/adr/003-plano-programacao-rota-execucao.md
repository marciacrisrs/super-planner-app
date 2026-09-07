# ADR 003 — Separação entre Plano, Programação, Rota e Execução

Status: aceite
Data: 2026-09-07

O Super Planner mantém quatro conceitos: Plano (orientação), Programação (intenção temporal), Rota (recomendação adaptativa) e Execução (registro do ocorrido). Alterar programação ou recalcular rota não altera o Plano original. O motor trabalha sobre ocorrências materializadas e a UI não duplica suas regras.
