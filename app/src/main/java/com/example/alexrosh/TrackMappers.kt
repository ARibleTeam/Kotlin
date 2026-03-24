package com.example.alexrosh

fun Track.toEntity(timestamp: Long = System.currentTimeMillis()): TrackEntity {
    return TrackEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        previewUrl = previewUrl,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        timestamp = timestamp
    )
}

fun TrackEntity.toTrack(): Track {
    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTimeMillis,
        previewUrl = previewUrl,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country
    )
}
