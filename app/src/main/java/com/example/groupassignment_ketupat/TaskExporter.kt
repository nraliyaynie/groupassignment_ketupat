package com.example.groupassignment_ketupat

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.OutputStream

object TaskExporter {

    fun exportTasks(context: Context, tasks: List<Task>) {
        val contentResolver = context.contentResolver

        val fileName = "task_backup_${System.currentTimeMillis()}.txt"

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
        }

        val uri = contentResolver.insert(
            MediaStore.Files.getContentUri("external"),
            values
        )

        uri?.let {
            val outputStream: OutputStream? =
                contentResolver.openOutputStream(it)

            val content = buildString {
                tasks.forEach {
                    append("${it.title} - ${it.status}\n")
                }
            }

            outputStream?.write(content.toByteArray())
            outputStream?.close()
        }
    }
}
