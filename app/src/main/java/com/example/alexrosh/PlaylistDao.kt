package com.example.alexrosh

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef): Long

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_track_cross_ref WHERE trackId = :trackId AND playlistId = :playlistId)")
    suspend fun isTrackInPlaylist(trackId: Long, playlistId: Long): Boolean

    @Query("UPDATE playlists SET tracksCount = tracksCount + 1 WHERE id = :playlistId")
    suspend fun incrementTrackCount(playlistId: Long)

    @Transaction
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackCrossRef): Boolean {
        if (isTrackInPlaylist(crossRef.trackId, crossRef.playlistId)) return false
        val rowId = insertCrossRef(crossRef)
        if (rowId != -1L) {
            incrementTrackCount(crossRef.playlistId)
            return true
        }
        return false
    }
}
