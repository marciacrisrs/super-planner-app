package com.superplanner.app.di

import com.superplanner.app.data.InMemoryRouteFeedbackRepository
import com.superplanner.app.domain.port.RouteFeedbackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RouteFeedbackModule {
    @Binds
    @Singleton
    abstract fun bindRouteFeedbackRepository(
        implementation: InMemoryRouteFeedbackRepository,
    ): RouteFeedbackRepository
}

