package com.example.alexrosh

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchBtn = findViewById<LinearLayout>(R.id.search_btn)
        val libraryBtn = findViewById<LinearLayout>(R.id.library_btn)
        val settingBtn = findViewById<LinearLayout>(R.id.settings_btn)

        // Обработчик для кнопки "Поиск" (лямбда)
        searchBtn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажата кнопка Поиск!", Toast.LENGTH_SHORT).show()
        }

        // Обработчик для кнопки "Медиатека" (лямбда)
        libraryBtn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажата кнопка Медиатека!", Toast.LENGTH_SHORT).show()
        }

        // Обработчик для кнопки "Настройки" (остался без изменений)
        settingBtn.setOnClickListener {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
    }
}