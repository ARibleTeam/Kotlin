package com.example.alexrosh

import android.content.SharedPreferences
import com.google.gson.Gson

const val SEARCH_HISTORY_PREFS = "search_history_prefs"
const val HISTORY_KEY = "history_key"
private const val MAX_HISTORY_SIZE = 10

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()

    fun read(): ArrayList<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return ArrayList()
        // Важно: toCollection(ArrayList()) для создания изменяемого списка
        return gson.fromJson(json, Array<Track>::class.java).toCollection(ArrayList())
    }

    private fun write(tracks: List<Track>) {
        val json = gson.toJson(tracks)
        sharedPreferences.edit()
            .putString(HISTORY_KEY, json)
            .apply()
    }

    fun addTrack(track: Track) {
        val history = read()
        history.removeIf { it.trackId == track.trackId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeLast()
        }
        write(history)
    }

    fun clear() {
        write(ArrayList())
    }
}