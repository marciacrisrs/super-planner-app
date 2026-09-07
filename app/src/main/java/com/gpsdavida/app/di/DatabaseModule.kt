package com.gpsdavida.app.di

import android.content.Context
import androidx.room.Room
import com.gpsdavida.app.data.local.*
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
    fun provideDatabase(@ApplicationContext context: Context): GpsDatabase =
        Room.databaseBuilder(context, GpsDatabase::class.java, "gps-da-vida.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides fun provideAppMetaDao(database: GpsDatabase): AppMetaDao = database.appMetaDao()
    @Provides fun provideEventDao(database: GpsDatabase): EventDao = database.eventDao()
    @Provides fun provideTaskDao(database: GpsDatabase): TaskDao = database.taskDao()
    @Provides fun provideHabitDao(database: GpsDatabase): HabitDao = database.habitDao()
    @Provides fun provideHabitCompletionDao(database: GpsDatabase): HabitCompletionDao = database.habitCompletionDao()
    @Provides fun provideRoutineDao(database: GpsDatabase): RoutineDao = database.routineDao()
    @Provides fun provideAvailabilityDao(database: GpsDatabase): AvailabilityDao = database.availabilityDao()
    @Provides fun provideActivityExecutionDao(database: GpsDatabase): ActivityExecutionDao = database.activityExecutionDao()
    @Provides fun provideGoalDao(database: GpsDatabase): GoalDao = database.goalDao()
    @Provides fun provideProjectDao(database: GpsDatabase): ProjectDao = database.projectDao()
    @Provides fun provideInboxItemDao(database: GpsDatabase): InboxItemDao = database.inboxItemDao()
    @Provides fun provideMilestoneDao(database: GpsDatabase): MilestoneDao = database.milestoneDao()
    @Provides fun provideFinanceDao(database: GpsDatabase): FinanceDao = database.financeDao()
    @Provides fun provideLifeAreaDao(database: GpsDatabase): LifeAreaDao = database.lifeAreaDao()
    @Provides fun providePlanDao(database: GpsDatabase): PlanDao = database.planDao()
    @Provides fun provideLeisureDao(database: GpsDatabase): LeisureDao = database.leisureDao()
}
