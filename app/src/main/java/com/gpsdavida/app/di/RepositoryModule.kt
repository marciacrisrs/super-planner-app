package com.superplanner.app.di

import com.superplanner.app.data.InMemoryRouteFeedbackRepository
import com.superplanner.app.data.RoomActivityExecutionRepository
import com.superplanner.app.data.RoomAvailabilityRepository
import com.superplanner.app.data.RoomEventRepository
import com.superplanner.app.data.RoomFinanceRepository
import com.superplanner.app.data.RoomGoalRepository
import com.superplanner.app.data.RoomHabitRepository
import com.superplanner.app.data.RoomInboxRepository
import com.superplanner.app.data.RoomLifeAreaRepository
import com.superplanner.app.data.RoomMilestoneRepository
import com.superplanner.app.data.RoomPlanRepository
import com.superplanner.app.data.RoomProjectRepository
import com.superplanner.app.data.RoomRoutineRepository
import com.superplanner.app.data.RoomTaskRepository
import com.superplanner.app.data.WeeklyPlanOverrideStore
import com.superplanner.app.domain.planning.DefaultPlanningEngine
import com.superplanner.app.domain.planning.PlanningEngine
import com.superplanner.app.domain.port.ActivityExecutionRepository
import com.superplanner.app.domain.port.AvailabilityRepository
import com.superplanner.app.domain.port.EventRepository
import com.superplanner.app.domain.port.FinanceRepository
import com.superplanner.app.domain.port.GoalRepository
import com.superplanner.app.domain.port.HabitRepository
import com.superplanner.app.domain.port.InboxRepository
import com.superplanner.app.domain.port.LifeAreaRepository
import com.superplanner.app.domain.port.MilestoneRepository
import com.superplanner.app.domain.port.PlanRepository
import com.superplanner.app.domain.port.ProjectRepository
import com.superplanner.app.domain.port.RoutineRepository
import com.superplanner.app.domain.port.TaskRepository
import com.superplanner.app.domain.port.WeeklyPlanOverrideRepository
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
    @Binds @Singleton abstract fun bindWeeklyPlanOverrideRepository(impl: WeeklyPlanOverrideStore): WeeklyPlanOverrideRepository
    @Binds @Singleton abstract fun bindPlanningEngine(impl: DefaultPlanningEngine): PlanningEngine
}
