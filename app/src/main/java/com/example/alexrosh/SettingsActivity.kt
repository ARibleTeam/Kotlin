package com.example.alexrosh

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Обновляем тип на FrameLayout
        val shareBtn = findViewById<FrameLayout>(R.id.share_btn)
        shareBtn.setOnClickListener {
            Toast.makeText(this, "Поделиться", Toast.LENGTH_SHORT).show()
        }

        // Добавляем обработчик для кнопки "Поддержка"
        val supportBtn = findViewById<FrameLayout>(R.id.support_btn)
        supportBtn.setOnClickListener {
            Toast.makeText(this, "Поддержка", Toast.LENGTH_SHORT).show()
        }

        // Добавляем обработчик для кнопки "Соглашение"
        val agreementBtn = findViewById<FrameLayout>(R.id.agreement_btn)
        agreementBtn.setOnClickListener {
            Toast.makeText(this, "Соглашение", Toast.LENGTH_SHORT).show()
        }
    }
}