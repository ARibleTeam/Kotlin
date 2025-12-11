package com.example.alexrosh

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // --- Логика переключателя темы ---
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.theme_switch)

        // Устанавливаем текущее состояние свитчера
        themeSwitcher.isChecked = (applicationContext as App).darkTheme

        // Вешаем слушатель на изменение состояния
        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
        }

        // --- Обработчики кнопок ---
        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val shareBtn = findViewById<FrameLayout>(R.id.share_btn)
        shareBtn.setOnClickListener {
            Toast.makeText(this, "Поделиться", Toast.LENGTH_SHORT).show()
        }

        val supportBtn = findViewById<FrameLayout>(R.id.support_btn)
        supportBtn.setOnClickListener {
            Toast.makeText(this, "Поддержка", Toast.LENGTH_SHORT).show()
        }

        val agreementBtn = findViewById<FrameLayout>(R.id.agreement_btn)
        agreementBtn.setOnClickListener {
            Toast.makeText(this, "Соглашение", Toast.LENGTH_SHORT).show()
        }
    }
}