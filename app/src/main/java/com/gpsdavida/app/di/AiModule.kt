package com.superplanner.app.di

import com.superplanner.app.data.DefaultAiToolGateway
import com.superplanner.app.domain.ai.AiProvider
import com.superplanner.app.domain.ai.RuleBasedAiProvider
import com.superplanner.app.domain.port.AiToolGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindAiProvider(implementation: RuleBasedAiProvider): AiProvider

    @Binds
    @Singleton
    abstract fun bindAiToolGateway(implementation: DefaultAiToolGateway): AiToolGateway
}