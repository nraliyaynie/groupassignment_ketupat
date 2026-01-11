package com.example.groupassignment_ketupat

import android.content.Context

class TaskRepository {

    private val fileName = "tasks.txt"

    fun saveTasks(context: Context, tasks: List<Task>) {
        val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
        tasks.forEach {
            val line = "${it.title}|${it.status}\n"
            fos.write(line.toByteArray())
        }
        fos.close()
    }

    fun loadTasks(context: Context): MutableList<Task> {
        val tasks = mutableListOf<Task>()

        try {
            val fis = context.openFileInput(fileName)
            val lines = fis.bufferedReader().readLines()
            fis.close()

            for (line in lines) {
                val parts = line.split("|")
                if (parts.size == 2) {
                    tasks.add(Task(parts[0], parts[1]))
                }
            }
        } catch (e: Exception) {
            // File belum wujud (first run)
        }

        return tasks
    }
}
