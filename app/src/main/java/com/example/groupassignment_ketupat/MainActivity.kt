package com.example.groupassignment_ketupat

import android.content.Intent
import android.os.Bundle
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var btnSettings: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskList: MutableList<Task>
    private lateinit var originalList: MutableList<Task>
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var spinnerSort: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {

        // 🔹 Role A: Apply saved theme
        val themeDataStore = ThemeDataStore(this)
        lifecycleScope.launch {
            val isDark = themeDataStore.darkModeFlow.first()
            AppCompatDelegate.setDefaultNightMode(
                if (isDark)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // INIT views
        btnSettings = findViewById(R.id.btnSettings)
        recyclerView = findViewById(R.id.recyclerTasks)
        fabAdd = findViewById(R.id.fabAdd)
        spinnerSort = findViewById(R.id.spinnerSort)

        //  Settings
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        //  Role B: Internal Storage
        taskRepository = TaskRepository()
        taskList = taskRepository.loadTasks(this)
        originalList = taskList.toMutableList()

        //  RecyclerView
        adapter = TaskAdapter(taskList) { position ->
            showEditDeleteDialog(position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        //  Spinner Sort
        spinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    when (position) {
                        0 -> { // Default
                            taskList.clear()
                            taskList.addAll(originalList)
                        }
                        1 -> { // Title A-Z
                            taskList.sortBy { it.title.lowercase() }
                        }
                        2 -> { // Status
                            taskList.sortBy { it.status.lowercase() }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        //  Add Task
        fabAdd.setOnClickListener {
            showAddTaskDialog()
        }
    }

    //  ADD TASK
    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.add_task, null)

        val edtTitle = dialogView.findViewById<EditText>(R.id.edtTitle)
        val edtStatus = dialogView.findViewById<EditText>(R.id.edtStatus)

        AlertDialog.Builder(this)
            .setTitle("Add Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = edtTitle.text.toString()
                val status = edtStatus.text.toString()

                if (title.isNotEmpty() && status.isNotEmpty()) {
                    val task = Task(title, status)
                    taskList.add(task)
                    originalList.add(task)
                    adapter.notifyItemInserted(taskList.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    //  EDIT / DELETE TASK
    private fun showEditDeleteDialog(position: Int) {
        val task = taskList[position]

        val dialogView = layoutInflater.inflate(R.layout.add_task, null)
        val edtTitle = dialogView.findViewById<EditText>(R.id.edtTitle)
        val edtStatus = dialogView.findViewById<EditText>(R.id.edtStatus)

        edtTitle.setText(task.title)
        edtStatus.setText(task.status)

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updated = Task(
                    edtTitle.text.toString(),
                    edtStatus.text.toString()
                )
                taskList[position] = updated
                originalList[position] = updated
                adapter.notifyItemChanged(position)
            }
            .setNegativeButton("Delete") { _, _ ->
                taskList.removeAt(position)
                originalList.removeAt(position)
                adapter.notifyItemRemoved(position)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    //  SAVE DATA
    override fun onPause() {
        super.onPause()
        taskRepository.saveTasks(this, taskList)
    }
}
