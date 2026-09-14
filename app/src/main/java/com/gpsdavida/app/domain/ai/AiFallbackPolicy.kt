package com.superplanner.app.domain.ai

import java.io.IOException

/** Keeps remote AI contract failures visible instead of masking them with local interpretation. */
internal object AiFallbackPolicy {
    fun shouldFallback(error: Throwable): Boolean = error is IOException
}
