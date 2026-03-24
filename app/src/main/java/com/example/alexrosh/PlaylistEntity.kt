package com.example.alexrosh

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val coverPath: String?,
    val tracksCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
