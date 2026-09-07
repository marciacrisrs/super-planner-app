package com.gpsdavida.app.data

import android.content.Context
import android.net.Uri
import com.gpsdavida.app.data.local.GpsDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject

class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: GpsDatabase,
) {
    fun exportTo(uri: Uri) {
        checkpoint()
        val source = context.getDatabasePath(DB_NAME)
        require(source.exists()) { "Database file does not exist" }
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            source.inputStream().use { input -> input.copyTo(output) }
        } ?: throw IOException("Unable to open backup destination")
    }

    fun restoreFrom(uri: Uri) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            database.close()
            val target = context.getDatabasePath(DB_NAME)
            target.parentFile?.mkdirs()
            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()
            target.outputStream().use { output -> input.copyTo(output) }
            database.openHelper.writableDatabase
        } ?: throw IOException("Unable to open backup source")
    }

    private fun checkpoint() {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    companion object { private const val DB_NAME = "gps-da-vida.db" }
}
