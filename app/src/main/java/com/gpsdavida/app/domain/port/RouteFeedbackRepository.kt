package com.superplanner.app.domain.port

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.RouteFeedback
import kotlinx.coroutines.flow.Flow

interface RouteFeedbackRepository {
    fun observeAll(): Flow<List<RouteFeedback>>
    fun observeFor(activityId: ActivityInstanceId): Flow<List<RouteFeedback>>
    suspend fun save(feedback: RouteFeedback)
}