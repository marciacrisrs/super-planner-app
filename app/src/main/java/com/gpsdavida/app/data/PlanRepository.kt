package com.superplanner.app.data

import com.superplanner.app.data.local.PlanDao
import com.superplanner.app.data.local.PlanEntity
import com.superplanner.app.data.local.PlanItemEntity
import com.superplanner.app.domain.model.Plan
import com.superplanner.app.domain.model.PlanItem
import com.superplanner.app.domain.model.PlanStatus
import com.superplanner.app.domain.model.PlanType
import com.superplanner.app.domain.model.RecurrenceRule
import com.superplanner.app.domain.port.PlanRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlanRepository @Inject constructor(private val dao: PlanDao) : PlanRepository {
    override fun observeAll(): Flow<List<Plan>> = dao.observePlans().map { rows -> rows.map(::toDomain) }
    override suspend fun getById(id: String): Plan? = dao.getPlan(id)?.let(::toDomain)
    override suspend fun save(plan: Plan) = dao.upsertPlan(toEntity(plan))
    override suspend fun delete(id: String) = dao.deletePlan(id)
    fun observeItems(planId: String): Flow<List<PlanItem>> = dao.observeItems(planId).map { it.map(::itemToDomain) }
    suspend fun saveItem(item: PlanItem) = dao.upsertItem(itemToEntity(item))
    suspend fun deleteItem(id: String) = dao.deleteItem(id)

    private fun toEntity(plan: Plan) = PlanEntity(
        plan.id, plan.name, plan.objective, plan.type.name, plan.origin, plan.status.name,
        plan.validFrom?.toString(), plan.validUntil?.toString(), plan.sourceDocument, plan.version,
    )

    private fun toDomain(row: PlanEntity) = Plan(
        row.id, row.name, row.objective,
        runCatching { PlanType.valueOf(row.type) }.getOrDefault(PlanType.GENERIC),
        row.origin, runCatching { PlanStatus.valueOf(row.status) }.getOrDefault(PlanStatus.ACTIVE),
        row.validFrom?.let(LocalDate::parse), row.validUntil?.let(LocalDate::parse), row.sourceDocument, row.version,
    )

    private fun itemToEntity(item: PlanItem) = PlanItemEntity(
        item.id, item.planId, item.title, item.durationMinutes,
        item.daysOfWeek.joinToString(",") { it.name }, item.time?.toString(), null, item.notes,
    )
    private fun itemToDomain(row: PlanItemEntity) = PlanItem(
        row.id, row.planId, row.title, row.durationMinutes,
        row.daysOfWeek.split(',').filter { it.isNotBlank() }.mapNotNull { runCatching { java.time.DayOfWeek.valueOf(it) }.getOrNull() }.toSet(),
        row.time?.let(LocalTime::parse), null, row.notes,
    )
}
