package com.superplanner.app.data

import android.util.Log
import com.superplanner.app.domain.ai.AiTelemetry
import javax.inject.Inject

/** Structured, privacy-safe diagnostics for QA and development builds. */
class LogcatAiTelemetry @Inject constructor() : AiTelemetry {
    override fun remoteSucceeded(commandType: String) = event("remote_success", commandType)

    override fun localFallback(reason: String) = event("local_fallback", reason)

    override fun remoteRejected(reason: String) = event("remote_rejected", reason)

    override fun proposalConfirmed(commandType: String) = event("proposal_confirmed", commandType)

    override fun executionSucceeded(commandType: String) = event("execution_success", commandType)

    override fun executionRejected(commandType: String) = event("execution_rejected", commandType)

    private fun event(name: String, value: String) {
        Log.i(TAG, "event=$name value=$value")
    }

    private companion object {
        const val TAG = "SuperPlannerAI"
    }
}
