package com.gpsdavida.app.di

import com.superplanner.app.data.DefaultAiToolGateway
import com.superplanner.app.data.LogcatAiTelemetry
import com.superplanner.app.domain.ai.AiProvider
import com.superplanner.app.domain.ai.AiTelemetry
import com.superplanner.app.domain.ai.HybridAiProvider
import com.superplanner.app.domain.port.AiToolGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AiModule {
    @Binds
    @Singleton
    fun bindAiProvider(implementation: HybridAiProvider): AiProvider

    @Binds
    @Singleton
    fun bindAiToolGateway(implementation: DefaultAiToolGateway): AiToolGateway

    @Binds
    @Singleton
    fun bindAiTelemetry(implementation: LogcatAiTelemetry): AiTelemetry
}
