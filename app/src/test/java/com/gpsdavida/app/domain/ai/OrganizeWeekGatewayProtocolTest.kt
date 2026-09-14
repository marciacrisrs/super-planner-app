package com.superplanner.app.domain.ai

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OrganizeWeekGatewayProtocolTest {
    @Test
    fun request_matches_gateway_contract_and_preserves_explicit_times() {
        val request = OrganizeWeekRequest(
            weekStart = "2026-09-14",
            timezone = "America/Sao_Paulo",
            existingPlan = listOf(
                OrganizeWeekPlanItem(
                    id = "work",
                    title = "Trabalho",
                    date = "2026-09-14",
                    startTime = "09:00",
                    endTime = "18:00",
                    durationMinutes = 540,
                    priority = "IMPORTANT",
                    kind = "work",
                    required = true,
                ),
            ),
            fixedCommitments = listOf(
                OrganizeWeekPlanItem(
                    id = "doctor",
                    title = "Consulta",
                    date = "2026-09-15",
                    startTime = "08:30",
                    durationMinutes = 60,
                    required = true,
                ),
            ),
            desires = listOf(
                OrganizeWeekPlanItem(
                    id = "french",
                    title = "Estudar francês",
                    date = "2026-09-16",
                    durationMinutes = 60,
                    priority = "IMPORTANT",
                ),
            ),
            logistics = listOf(
                OrganizeWeekLogisticConstraint(
                    type = "COMMUTE",
                    minutes = 45,
                    beforeItemId = "work",
                    origin = "home",
                    destination = "office",
                ),
            ),
            preferences = listOf(OrganizeWeekPreference("morning", "exercise")),
            aiTips = listOf("Reserve espaço para descanso."),
        )

        val json = OrganizeWeekGatewayProtocol.buildRequestBody(request)

        assertEquals("2026-09-14", json.getString("weekStart"))
        assertEquals("America/Sao_Paulo", json.getString("timezone"))
        assertEquals("08:30", json.getJSONArray("fixedCommitments").getJSONObject(0).getString("startTime"))
        assertEquals(45, json.getJSONArray("logistics").getJSONObject(0).getInt("minutes"))
        assertEquals("COMMUTE", json.getJSONArray("logistics").getJSONObject(0).getString("type"))
        assertEquals("Reserve espaço para descanso.", json.getJSONArray("aiTips").getString(0))
        assertFalse(json.toString().contains("password", ignoreCase = true))
        assertFalse(json.toString().contains("api_key", ignoreCase = true))
    }

    @Test
    fun request_omits_null_optional_fields() {
        val json = OrganizeWeekGatewayProtocol.buildRequestBody(
            OrganizeWeekRequest(
                weekStart = "2026-09-14",
                timezone = "America/Sao_Paulo",
                existingPlan = listOf(
                    OrganizeWeekPlanItem("x", "Tarefa", "2026-09-14"),
                ),
            ),
        )

        val item = json.getJSONArray("existingPlan").getJSONObject(0)
        assertNull(item.optString("startTime").takeIf(String::isNotBlank))
        assertNull(item.optString("endTime").takeIf(String::isNotBlank))
        assertNull(item.optString("priority").takeIf(String::isNotBlank))
        assertFalse(item.has("startTime"))
        assertFalse(item.has("durationMinutes"))
    }

    @Test
    fun response_parser_maps_summary_proposal_conflict_opportunity_and_explanation() {
        val response = OrganizeWeekGatewayProtocol.parseResponse(
            JSONObject(
                """
                {
                  "summary": {
                    "fixedCommitmentsConsidered": 2,
                    "desiresConsidered": 3,
                    "commuteMinutesConsidered": 90,
                    "preparationMinutesConsidered": 45,
                    "aiSuggestionsConsidered": 1,
                    "conflictsFound": 1,
                    "opportunitiesFound": 2
                  },
                  "proposedItems": [
                    {
                      "id":"doctor",
                      "title":"Consulta",
                      "date":"2026-09-15",
                      "startTime":"08:30",
                      "endTime":"09:30",
                      "source":"fixed",
                      "fixed":true,
                      "reason":"Compromisso fixo"
                    }
                  ],
                  "conflicts": [
                    {
                      "id":"c1",
                      "title":"Conflito",
                      "affectedItemIds":["doctor","exercise"],
                      "reason":"Horários sobrepostos",
                      "severity":"high"
                    }
                  ],
                  "opportunities": [
                    {
                      "id":"o1",
                      "title":"Espaço livre",
                      "reason":"Há capacidade pela manhã"
                    }
                  ],
                  "explanations": [
                    {
                      "itemId":"doctor",
                      "message":"Reservei o deslocamento antes da consulta."
                    },
                    {
                      "itemId":null,
                      "message":"Preservei os compromissos fixos."
                    }
                  ],
                  "model":"gemini-test"
                }
                """.trimIndent(),
            ),
        )

        assertEquals(2, response.summary.fixedCommitmentsConsidered)
        assertEquals(90, response.summary.commuteMinutesConsidered)
        assertEquals(1, response.proposedItems.size)
        assertTrue(response.proposedItems.single().fixed)
        assertEquals("fixed", response.proposedItems.single().source)
        assertEquals("high", response.conflicts.single().severity)
        assertEquals(listOf("doctor", "exercise"), response.conflicts.single().affectedItemIds)
        assertEquals("Espaço livre", response.opportunities.single().title)
        assertEquals(2, response.explanations.size)
        assertNull(response.explanations[1].itemId)
        assertEquals("gemini-test", response.model)
    }
}
