package com.gpsdavida.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppMetaEntity::class, EventEntity::class, TaskEntity::class, HabitEntity::class,
        HabitCompletionEntity::class, RoutineEntity::class, RoutineStepEntity::class,
        AvailabilityEntity::class, ActivityExecutionEntity::class, GoalEntity::class,
        ProjectEntity::class, InboxItemEntity::class, MilestoneEntity::class,
        AccountEntity::class, TransactionEntity::class, PayableEntity::class,
        BudgetEntity::class, FinancialGoalEntity::class,
    ],
    version = 10,
    exportSchema = false,
)
abstract class GpsDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun eventDao(): EventDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun routineDao(): RoutineDao
    abstract fun availabilityDao(): AvailabilityDao
    abstract fun activityExecutionDao(): ActivityExecutionDao
    abstract fun goalDao(): GoalDao
    abstract fun projectDao(): ProjectDao
    abstract fun inboxItemDao(): InboxItemDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun financeDao(): FinanceDao
}
