package com.gpsdavida.app.data

import android.content.Context
import android.net.Uri
import androidx.room.RoomDatabase
import java.io.IOException
import javax.inject.Inject

class BackupManager @Inject constructor(
    private val context: Context,
    private val database: RoomDatabase,
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
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open backup source")
        input.use {
            database.close()
            val target = context.getDatabasePath(DB_NAME)
            target.parentFile?.mkdirs()
            context.getDatabasePath("$DB_NAME-wal").delete()
            context.getDatabasePath("$DB_NAME-shm").delete()
            target.outputStream().use { output -> it.copyTo(output) }
            database.openHelper.writableDatabase
        }
    }

    private fun checkpoint() {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
    }

    companion object { private const val DB_NAME = "gps-da-vida.db" }
}
