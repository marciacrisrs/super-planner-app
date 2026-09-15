package com.superplanner.app.di

import com.superplanner.app.data.*
import com.superplanner.app.domain.planning.DefaultPlanningEngine
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.port.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds @Singleton fun bindActivityExecutionRepository(impl: RoomActivityExecutionRepository): ActivityExecutionRepository
    @Binds @Singleton fun bindEventRepository(impl: RoomEventRepository): EventRepository
    @Binds @Singleton fun bindTaskRepository(impl: RoomTaskRepository): TaskRepository
    @Binds @Singleton fun bindHabitRepository(impl: RoomHabitRepository): HabitRepository
    @Binds @Singleton fun bindRoutineRepository(impl: RoomRoutineRepository): RoutineRepository
    @Binds @Singleton fun bindAvailabilityRepository(impl: RoomAvailabilityRepository): AvailabilityRepository
    @Binds @Singleton fun bindGoalRepository(impl: RoomGoalRepository): GoalRepository
    @Binds @Singleton fun bindProjectRepository(impl: RoomProjectRepository): ProjectRepository
    @Binds @Singleton fun bindInboxRepository(impl: RoomInboxRepository): InboxRepository
    @Binds @Singleton fun bindMilestoneRepository(impl: RoomMilestoneRepository): MilestoneRepository
    @Binds @Singleton fun bindFinanceRepository(impl: RoomFinanceRepository): FinanceRepository
    @Binds @Singleton fun bindLifeAreaRepository(impl: RoomLifeAreaRepository): LifeAreaRepository
    @Binds @Singleton fun bindPlanRepository(impl: RoomPlanRepository): PlanRepository
    @Binds @Singleton fun bindWeeklyPlanOverrideRepository(impl: WeeklyPlanOverrideStore): WeeklyPlanOverrideRepository
    @Binds @Singleton fun bindPlanningEngine(impl: DefaultPlanningEngine): PlanningEngine
}
