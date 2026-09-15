package com.superplanner.app.data

import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.RouteFeedback
import com.superplanner.app.domain.port.RouteFeedbackRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@Singleton
class InMemoryRouteFeedbackRepository @Inject constructor() : RouteFeedbackRepository {
    private val feedback = MutableStateFlow<List<RouteFeedback>>(emptyList())

    override fun observeAll(): Flow<List<RouteFeedback>> = feedback

    override fun observeFor(activityId: ActivityInstanceId): Flow<List<RouteFeedback>> =
        feedback.map { entries -> entries.filter { it.activityId == activityId } }

    override suspend fun save(feedback: RouteFeedback) {
        this.feedback.update { current -> current + feedback }
    }
}
