package com.superplanner.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.superplanner.app.domain.model.ActivityStatus
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityExecutionDaoTest {
    private lateinit var database: SuperPlannerDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SuperPlannerDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `execution survives dao round trip`() = runTest {
        val entity = ActivityExecutionEntity(
            activityInstanceId = "activity-1",
            status = ActivityStatus.DONE.name,
            plannedStart = "2026-01-01T09:00:00Z",
            plannedEnd = "2026-01-01T10:00:00Z",
            actualStart = "2026-01-01T09:05:00Z",
            actualEnd = "2026-01-01T10:30:00Z",
        )

        database.activityExecutionDao().upsert(entity)
        val loaded = database.activityExecutionDao().getById("activity-1")

        assertNotNull(loaded)
        assertEquals(ActivityStatus.DONE.name, loaded!!.status)
        assertEquals(Instant.parse(requireNotNull(entity.actualStart)), Instant.parse(requireNotNull(loaded.actualStart)))
        assertEquals(Instant.parse(requireNotNull(entity.actualEnd)), Instant.parse(requireNotNull(loaded.actualEnd)))
    }
}
