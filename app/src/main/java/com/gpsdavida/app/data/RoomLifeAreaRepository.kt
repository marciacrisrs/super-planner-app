package com.superplanner.app.data

import com.superplanner.app.data.local.LifeAreaDao
import com.superplanner.app.data.local.LifeAreaEntity
import com.superplanner.app.domain.model.LifeArea
import com.superplanner.app.domain.port.LifeAreaRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomLifeAreaRepository @Inject constructor(private val dao: LifeAreaDao) : LifeAreaRepository {
    override fun observeAll(): Flow<List<LifeArea>> = dao.observeAll().map { it.map { row -> LifeArea(row.id, row.name, row.colorKey) } }
    override suspend fun save(area: LifeArea) = dao.upsert(LifeAreaEntity(area.id, area.name, area.colorKey))
    override suspend fun delete(id: String) = dao.delete(id)
}
