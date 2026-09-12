package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Dependency
import com.superplanner.app.domain.model.DependencyId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DependencyGraphTest {
    private val a = ActivitySource("a")
    private val b = ActivitySource("b")
    private val c = ActivitySource("c")

    @Test fun blockedUntilPredecessorDone() {
        val graph = DependencyGraph(listOf(Dependency(DependencyId("1"), a, b)))
        assertTrue(graph.isBlocked(b, emptySet()))
        assertFalse(graph.isBlocked(b, setOf(a)))
    }

    @Test fun rejectsDirectCycle() {
        val graph = DependencyGraph(listOf(Dependency(DependencyId("1"), a, b)))
        assertTrue(graph.wouldCreateCycle(Dependency(DependencyId("2"), b, a)))
    }

    @Test fun rejectsTransitiveCycle() {
        val graph = DependencyGraph(listOf(
            Dependency(DependencyId("1"), a, b),
            Dependency(DependencyId("2"), b, c),
        ))
        assertTrue(graph.wouldCreateCycle(Dependency(DependencyId("3"), c, a)))
    }
}
