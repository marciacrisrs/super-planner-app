package com.superplanner.app.di

import com.superplanner.app.data.*
import com.superplanner.app.domain.port.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindActivityExecutionRepository(impl: RoomActivityExecutionRepository): ActivityExecutionRepository
    @Binds @Singleton abstract fun bindEventRepository(impl: RoomEventRepository): EventRepository
    @Binds @Singleton abstract fun bindTaskRepository(impl: RoomTaskRepository): TaskRepository
    @Binds @Singleton abstract fun bindHabitRepository(impl: RoomHabitRepository): HabitRepository
    @Binds @Singleton abstract fun bindRoutineRepository(impl: RoomRoutineRepository): RoutineRepository
    @Binds @Singleton abstract fun bindAvailabilityRepository(impl: RoomAvailabilityRepository): AvailabilityRepository
    @Binds @Singleton abstract fun bindGoalRepository(impl: RoomGoalRepository): GoalRepository
    @Binds @Singleton abstract fun bindProjectRepository(impl: RoomProjectRepository): ProjectRepository
    @Binds @Singleton abstract fun bindInboxRepository(impl: RoomInboxRepository): InboxRepository
    @Binds @Singleton abstract fun bindMilestoneRepository(impl: RoomMilestoneRepository): MilestoneRepository
    @Binds @Singleton abstract fun bindFinanceRepository(impl: RoomFinanceRepository): FinanceRepository
    @Binds @Singleton abstract fun bindLifeAreaRepository(impl: RoomLifeAreaRepository): LifeAreaRepository
    @Binds @Singleton abstract fun bindPlanRepository(impl: RoomPlanRepository): PlanRepository
}
