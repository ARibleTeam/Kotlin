package com.example.alexrosh

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import android.content.SharedPreferences
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val THEME_PREFS = "theme_prefs"
const val DARK_THEME_KEY = "dark_theme_key"

class App : Application() {

    var darkTheme = false
    private lateinit var sharedPrefs: SharedPreferences
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).addMigrations(MIGRATION_1_2).build()
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

    companion object {
        private const val DATABASE_NAME = "playlist_database.db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS playlists (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        coverPath TEXT,
                        tracksCount INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS playlist_track_cross_ref (
                        playlistId INTEGER NOT NULL,
                        trackId INTEGER NOT NULL,
                        PRIMARY KEY(playlistId, trackId),
                        FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_playlist_track_cross_ref_playlistId ON playlist_track_cross_ref(playlistId)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_playlist_track_cross_ref_trackId ON playlist_track_cross_ref(trackId)"
                )
            }
        }
    }
}