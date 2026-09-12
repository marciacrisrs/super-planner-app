package com.superplanner.app.domain.usecase

import com.superplanner.app.domain.model.ActivitySource
import com.superplanner.app.domain.model.Dependency

class DependencyGraph(private val dependencies: List<Dependency>) {
    fun isBlocked(activity: ActivitySource, completed: Set<ActivitySource>): Boolean =
        dependencies.any { it.successor == activity && it.predecessor !in completed }

    fun wouldCreateCycle(candidate: Dependency): Boolean {
        val next = dependencies + candidate
        fun reachable(from: ActivitySource, target: ActivitySource, visited: Set<ActivitySource>): Boolean {
            if (from == target) return true
            if (from in visited) return false
            return next.filter { it.predecessor == from }.any { reachable(it.successor, target, visited + from) }
        }
        return reachable(candidate.successor, candidate.predecessor, emptySet())
    }
}
