package com.superplanner.app.di

import android.content.Context
import androidx.room.Room
import com.superplanner.app.data.local.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SuperPlannerDatabase =
        Room.databaseBuilder(context, SuperPlannerDatabase::class.java, "gps-da-vida.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideAppMetaDao(database: SuperPlannerDatabase): AppMetaDao = database.appMetaDao()
    @Provides fun provideEventDao(database: SuperPlannerDatabase): EventDao = database.eventDao()
    @Provides fun provideTaskDao(database: SuperPlannerDatabase): TaskDao = database.taskDao()
    @Provides fun provideHabitDao(database: SuperPlannerDatabase): HabitDao = database.habitDao()
    @Provides fun provideHabitCompletionDao(database: SuperPlannerDatabase): HabitCompletionDao = database.habitCompletionDao()
    @Provides fun provideRoutineDao(database: SuperPlannerDatabase): RoutineDao = database.routineDao()
    @Provides fun provideAvailabilityDao(database: SuperPlannerDatabase): AvailabilityDao = database.availabilityDao()
    @Provides fun provideActivityExecutionDao(database: SuperPlannerDatabase): ActivityExecutionDao = database.activityExecutionDao()
    @Provides fun provideGoalDao(database: SuperPlannerDatabase): GoalDao = database.goalDao()
    @Provides fun provideProjectDao(database: SuperPlannerDatabase): ProjectDao = database.projectDao()
    @Provides fun provideInboxItemDao(database: SuperPlannerDatabase): InboxItemDao = database.inboxItemDao()
    @Provides fun provideMilestoneDao(database: SuperPlannerDatabase): MilestoneDao = database.milestoneDao()
    @Provides fun provideFinanceDao(database: SuperPlannerDatabase): FinanceDao = database.financeDao()
    @Provides fun provideLifeAreaDao(database: SuperPlannerDatabase): LifeAreaDao = database.lifeAreaDao()
    @Provides fun providePlanDao(database: SuperPlannerDatabase): PlanDao = database.planDao()
    @Provides fun provideLeisureDao(database: SuperPlannerDatabase): LeisureDao = database.leisureDao()
}
