package com.example.alexrosh

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks WHERE trackId = :id LIMIT 1")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM favorite_tracks ORDER BY timestamp DESC")
    suspend fun getAllTracks(): List<TrackEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks WHERE trackId = :id)")
    suspend fun isFavorite(id: Long): Boolean
}
