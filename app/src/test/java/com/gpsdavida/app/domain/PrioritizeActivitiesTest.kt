package com.superplanner.app.domain

import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.DelayConsequence
import com.superplanner.app.domain.model.Flexibility
import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.model.TaskId
import com.superplanner.app.domain.model.TimeRange
import com.superplanner.app.domain.usecase.PrioritizeActivities
import com.superplanner.app.domain.usecase.isMandatory
import com.superplanner.app.domain.usecase.weight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class PrioritizeActivitiesTest {
    private val prioritize = PrioritizeActivities()

    @Test
    fun `priority weights are ordered from required to leisure`() {
        assertTrue(Priority.REQUIRED.weight < Priority.IMPORTANT.weight)
        assertTrue(Priority.IMPORTANT.weight < Priority.DESIRABLE.weight)
        assertTrue(Priority.DESIRABLE.weight < Priority.LEISURE.weight)
    }

    @Test
    fun `activities are sorted by priority`() {
        val items = listOf(
            Item("leisure", Priority.LEISURE),
            Item("desirable", Priority.DESIRABLE),
            Item("required", Priority.REQUIRED),
            Item("important", Priority.IMPORTANT),
        )

        assertEquals(
            listOf("required", "important", "desirable", "leisure"),
            prioritize(items) { it.priority }.map { it.name },
        )
    }

    @Test
    fun `equal priority preserves input order`() {
        val items = listOf(
            Item("first", Priority.IMPORTANT),
            Item("second", Priority.IMPORTANT),
            Item("third", Priority.REQUIRED),
        )

        assertEquals(
            listOf("third", "first", "second"),
            prioritize(items) { it.priority }.map { it.name },
        )
    }

    @Test
    fun `required is mandatory and other priorities are flexible`() {
        assertTrue(Priority.REQUIRED.isMandatory)
        assertFalse(Priority.IMPORTANT.isMandatory)
        assertFalse(Priority.DESIRABLE.isMandatory)
        assertFalse(Priority.LEISURE.isMandatory)
    }

    @Test
    fun `fixed anchors are protected before flexible activities`() {
        val items = listOf(
            activity("important", priority = Priority.IMPORTANT),
            activity("anchor", flexibility = Flexibility.FIXED, priority = Priority.LEISURE),
            activity("required", priority = Priority.REQUIRED),
        )

        assertEquals(
            listOf("anchor", "required", "important"),
            prioritize(items).map { it.id.value.removePrefix("activity-") },
        )
    }

    @Test
    fun `deadline is used only after explicit priority`() {
        val items = listOf(
            activity(
                "important-later",
                priority = Priority.IMPORTANT,
                dueAt = Instant.parse("2026-09-20T10:00:00Z"),
            ),
            activity(
                "important-sooner",
                priority = Priority.IMPORTANT,
                dueAt = Instant.parse("2026-09-14T10:00:00Z"),
            ),
            activity(
                "required-later",
                priority = Priority.REQUIRED,
                dueAt = Instant.parse("2026-09-30T10:00:00Z"),
            ),
        )

        assertEquals(
            listOf("required-later", "important-sooner", "important-later"),
            prioritize(items).map { it.id.value.removePrefix("activity-") },
        )
    }

    @Test
    fun `consequence of delay breaks ties after priority and deadline`() {
        val dueAt = Instant.parse("2026-09-15T10:00:00Z")
        val items = listOf(
            activity(
                "low-consequence",
                priority = Priority.IMPORTANT,
                dueAt = dueAt,
                delayConsequence = DelayConsequence.LOW,
            ),
            activity(
                "critical-consequence",
                priority = Priority.IMPORTANT,
                dueAt = dueAt,
                delayConsequence = DelayConsequence.CRITICAL,
            ),
        )

        assertEquals(
            listOf("critical-consequence", "low-consequence"),
            prioritize(items).map { it.id.value.removePrefix("activity-") },
        )
    }

    private fun activity(
        name: String,
        flexibility: Flexibility = Flexibility.FLEXIBLE,
        priority: Priority = Priority.IMPORTANT,
        dueAt: Instant? = null,
        delayConsequence: DelayConsequence = DelayConsequence.NONE,
    ) = ActivityInstance(
        id = ActivityInstanceId("activity-$name"),
        source = ActivitySource.FromTask(TaskId("task-$name")),
        flexibility = flexibility,
        planned = TimeRange(
            Instant.parse("2026-09-13T09:00:00Z"),
            Instant.parse("2026-09-13T10:00:00Z"),
        ),
        priority = priority,
        dueAt = dueAt,
        delayConsequence = delayConsequence,
    )

    private data class Item(val name: String, val priority: Priority)
}
