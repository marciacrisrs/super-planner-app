package com.superplanner.app.domain.ai

import java.io.IOException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiFallbackPolicyTest {
    @Test
    fun `falls back only when remote transport is unavailable`() {
        assertTrue(AiFallbackPolicy.shouldFallback(IOException("timeout")))
    }

    @Test
    fun `does not hide gateway contract errors`() {
        assertFalse(AiFallbackPolicy.shouldFallback(IllegalArgumentException("unsupported schema")))
        assertFalse(AiFallbackPolicy.shouldFallback(IllegalStateException("invalid proposal")))
    }
}
