package com.example.alexrosh

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences

const val THEME_PREFS = "theme_prefs"
const val DARK_THEME_KEY = "dark_theme_key"

class App : Application() {

    var darkTheme = false
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(THEME_PREFS, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(DARK_THEME_KEY, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(DARK_THEME_KEY, darkTheme)
            .apply()
    }
}