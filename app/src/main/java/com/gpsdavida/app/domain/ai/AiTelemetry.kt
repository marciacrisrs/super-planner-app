package com.superplanner.app.domain.ai

/**
 * Privacy-safe AI lifecycle events. Implementations must never record the user's message,
 * route facts, proposal text, or other free-form personal content.
 */
interface AiTelemetry {
    fun remoteSucceeded(commandType: String)
    fun localFallback(reason: String)
    fun remoteRejected(reason: String)
    fun proposalConfirmed(commandType: String)
    fun executionSucceeded(commandType: String)
    fun executionRejected(commandType: String)
}

object NoOpAiTelemetry : AiTelemetry {
    override fun remoteSucceeded(commandType: String) = Unit
    override fun localFallback(reason: String) = Unit
    override fun remoteRejected(reason: String) = Unit
    override fun proposalConfirmed(commandType: String) = Unit
    override fun executionSucceeded(commandType: String) = Unit
    override fun executionRejected(commandType: String) = Unit
}
