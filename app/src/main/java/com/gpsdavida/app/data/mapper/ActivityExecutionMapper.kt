package com.superplanner.app.data.mapper

import com.superplanner.app.data.local.ActivityExecutionEntity
import com.superplanner.app.domain.model.ActivityExecution
import com.superplanner.app.domain.model.ActivityInstance
import com.superplanner.app.domain.model.ActivityInstanceId
import com.superplanner.app.domain.model.ActivityStatus
import com.superplanner.app.domain.model.TimeRange
import java.time.Instant

fun ActivityInstance.toExecutionEntity(): ActivityExecutionEntity = ActivityExecutionEntity(
    activityInstanceId = id.value,
    status = status.name,
    plannedStart = planned.start.toString(),
    plannedEnd = planned.end.toString(),
    actualStart = actual?.start?.toString(),
    actualEnd = actual?.end?.toString(),
)

fun ActivityExecutionEntity.toDomain(): ActivityExecution = ActivityExecution(
    activityInstanceId = ActivityInstanceId(activityInstanceId),
    status = ActivityStatus.valueOf(status),
    planned = TimeRange(Instant.parse(plannedStart), Instant.parse(plannedEnd)),
    actual = if (actualStart != null && actualEnd != null) {
        TimeRange(Instant.parse(actualStart), Instant.parse(actualEnd))
    } else {
        null
    },
)
