package com.superplanner.app.ui.widget

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgoraWidgetSnapshotTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun writeAndRead_preservesNextAction() {
        val snapshot = AgoraWidgetSnapshot(
            title = "Estudar francês",
            scheduledTime = "18:30",
            durationMinutes = 30,
            isEmpty = false,
        )

        AgoraWidgetSnapshot.write(context, snapshot)

        assertEquals(snapshot, AgoraWidgetSnapshot.read(context))
        assertFalse(AgoraWidgetSnapshot.read(context).isEmpty)
    }

    @Test
    fun writeEmptyState_persistsEmptyWidget() {
        AgoraWidgetSnapshot.write(
            context,
            AgoraWidgetSnapshot("", "", 0, isEmpty = true),
        )

        val result = AgoraWidgetSnapshot.read(context)
        assertTrue(result.isEmpty)
        assertEquals("", result.title)
    }
}
