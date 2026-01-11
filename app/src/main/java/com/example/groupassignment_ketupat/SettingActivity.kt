package com.example.groupassignment_ketupat

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingActivity : AppCompatActivity() {

    private lateinit var btnBackup: Button
    private lateinit var switchDarkMode: Switch
    private lateinit var themeDataStore: ThemeDataStore
    private lateinit var taskRepository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        // ✅ INIT ALL VIEWS FIRST
        switchDarkMode = findViewById(R.id.switchDarkMode)
        btnBackup = findViewById(R.id.btnBackup)

        themeDataStore = ThemeDataStore(this)
        taskRepository = TaskRepository()

        // ✅ Load saved dark mode (DataStore)
        lifecycleScope.launch {
            val isDark = themeDataStore.darkModeFlow.first()
            switchDarkMode.isChecked = isDark
        }

        // ✅ Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                themeDataStore.saveDarkMode(isChecked)
            }

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // 🔹 ROLE C: Backup button
        btnBackup.setOnClickListener {
            val tasks = taskRepository.loadTasks(this)
            TaskExporter.exportTasks(this, tasks)
            Toast.makeText(
                this,
                "Backup saved to Downloads",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}