package com.superplanner.app.domain.ai

import com.superplanner.app.domain.model.Energy
import com.superplanner.app.domain.model.Priority
import java.time.Duration
import java.time.LocalDate
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiGatewayProposalMapperTest {
    @Test
    fun `maps Gateway JSON into draft without losing explicit date or start time`() {
        val request = AiRequest(
            message = "Quero estudar francês amanhã às 18h por uma hora",
            context = AiContext(nowIso = "2026-09-13T21:00:00-03:00"),
        )
        val payload = JSONObject(
            """
            {
              "title":"Estudar francês",
              "durationMinutes":60,
              "date":"2026-09-14",
              "startTime":"18:00",
              "priority":"IMPORTANT",
              "energy":"HIGH"
            }
            """.trimIndent(),
        )

        val draft = AiGatewayProposalMapper.mapCreateActivityDraft(payload, request)

        assertEquals("Estudar francês", draft.title)
        assertEquals(Duration.ofHours(1), draft.plannedDuration)
        assertEquals(LocalDate.of(2026, 9, 14), draft.date)
        assertEquals(java.time.LocalTime.of(18, 0), draft.startTime)
        assertEquals(Priority.IMPORTANT, draft.priority)
        assertEquals(Energy.HIGH, draft.energy)
        assertEquals(emptySet<MissingActivityField>(), draft.missingFields)
    }

    @Test
    fun `Gateway may omit optional start time without inventing one`() {
        val request = AiRequest(
            message = "Estudar francês amanhã por uma hora",
            context = AiContext(nowIso = "2026-09-13T21:00:00-03:00"),
        )
        val payload = JSONObject(
            """
            {
              "title":"Estudar francês",
              "durationMinutes":60,
              "date":"2026-09-14",
              "startTime":"",
              "priority":"IMPORTANT",
              "energy":null
            }
            """.trimIndent(),
        )

        val draft = AiGatewayProposalMapper.mapCreateActivityDraft(payload, request)

        assertEquals(LocalDate.of(2026, 9, 14), draft.date)
        assertNull(draft.startTime)
    }
}
