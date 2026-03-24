package com.example.alexrosh

import android.content.Context

fun Context.getTracksCountText(count: Int): String {
    val absCount = kotlin.math.abs(count) % 100
    val lastDigit = absCount % 10
    val template = when {
        absCount in 11..14 -> R.string.playlist_tracks_count_many
        lastDigit == 1 -> R.string.playlist_tracks_count_single
        lastDigit in 2..4 -> R.string.playlist_tracks_count_few
        else -> R.string.playlist_tracks_count_many
    }
    return getString(template, count)
}
