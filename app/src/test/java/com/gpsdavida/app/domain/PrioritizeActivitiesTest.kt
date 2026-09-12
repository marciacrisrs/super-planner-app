package com.superplanner.app.domain

import com.superplanner.app.domain.model.Priority
import com.superplanner.app.domain.usecase.PrioritizeActivities
import com.superplanner.app.domain.usecase.isMandatory
import com.superplanner.app.domain.usecase.weight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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

    private data class Item(val name: String, val priority: Priority)
}
