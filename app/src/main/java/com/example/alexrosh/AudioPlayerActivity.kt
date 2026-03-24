package com.example.alexrosh

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.gson.Gson
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioPlayerActivity : AppCompatActivity() {

    private val gson = Gson()
    private val mediaPlayer = MediaPlayer()
    private val playerHandler = Handler(Looper.getMainLooper())

    private lateinit var playButton: ImageButton
    private lateinit var currentTimeValue: TextView
    private lateinit var likeButton: ImageButton
    private lateinit var currentTrack: Track
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var playlistsAdapter: PlaylistBottomSheetAdapter

    private var playerState = STATE_DEFAULT
    private var isFavorite = false
    private val progressRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                currentTimeValue.text =
                    SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                playerHandler.postDelayed(this, TIMER_REFRESH_DELAY_MILLIS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        val track = intent.getStringExtra(TRACK_JSON_KEY)?.let { json ->
            gson.fromJson(json, Track::class.java)
        }

        if (track == null) {
            finish()
            return
        }

        currentTrack = track
        setupViews(track)
        preparePlayer(track.previewUrl)
        checkFavoriteState(track.trackId)
        setupBackHandler()
    }

    private fun setupViews(track: Track) {
        val backButton = findViewById<ImageView>(R.id.back_button)
        val coverArtwork = findViewById<ImageView>(R.id.cover_artwork)
        val trackName = findViewById<TextView>(R.id.track_name)
        val artistName = findViewById<TextView>(R.id.artist_name)
        val albumLabel = findViewById<TextView>(R.id.album_label)
        val albumValue = findViewById<TextView>(R.id.album_value)
        val yearValue = findViewById<TextView>(R.id.year_value)
        val genreValue = findViewById<TextView>(R.id.genre_value)
        val countryValue = findViewById<TextView>(R.id.country_value)
        val durationValue = findViewById<TextView>(R.id.duration_value)
        currentTimeValue = findViewById(R.id.current_time_value)
        val addToPlaylistButton = findViewById<ImageButton>(R.id.add_to_playlist_button)
        val newPlaylistButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.new_playlist_button)
        val playlistBottomSheet = findViewById<LinearLayout>(R.id.playlist_bottom_sheet)
        val playlistsRecycler = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.playlists_bottom_sheet_recycler)
        playButton = findViewById(R.id.play_button)
        likeButton = findViewById(R.id.like_button)

        backButton.setOnClickListener { finish() }

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(coverArtwork)

        trackName.text = track.trackName
        artistName.text = track.artistName
        yearValue.text = track.releaseDate?.take(4).orEmpty()
        genreValue.text = track.primaryGenreName.orEmpty()
        countryValue.text = track.country.orEmpty()
        val formattedTrackTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
        durationValue.text = formattedTrackTime
        currentTimeValue.text = getString(R.string.audio_player_current_time_default)

        if (track.collectionName.isNullOrEmpty()) {
            albumLabel.visibility = View.GONE
            albumValue.visibility = View.GONE
        } else {
            albumLabel.visibility = View.VISIBLE
            albumValue.visibility = View.VISIBLE
            albumValue.text = track.collectionName
        }

        addToPlaylistButton.setImageResource(R.drawable.ic_playlist_add)
        playButton.setImageResource(R.drawable.ic_play)
        renderLikeButton()

        val secondaryIconTint = ContextCompat.getColor(this, R.color.audio_player_icon_on_secondary)
        val primaryIconTint = ContextCompat.getColor(this, R.color.audio_player_icon_on_primary)
        addToPlaylistButton.imageTintList = ColorStateList.valueOf(secondaryIconTint)
        playButton.imageTintList = ColorStateList.valueOf(primaryIconTint)
        likeButton.imageTintList = null

        playButton.isEnabled = false
        currentTimeValue.text = getString(R.string.audio_player_current_time_default)

        setupBottomSheet(playlistBottomSheet, playlistsRecycler)
        addToPlaylistButton.setOnClickListener { showBottomSheet() }
        newPlaylistButton.setOnClickListener {
            startActivity(NewPlaylistActivity.createIntent(this))
        }
        playButton.setOnClickListener { playbackControl() }
        likeButton.setOnClickListener { onLikeClicked() }
    }

    private fun setupBottomSheet(
        bottomSheet: LinearLayout,
        playlistsRecycler: androidx.recyclerview.widget.RecyclerView
    ) {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            skipCollapsed = false
        }
        playlistsAdapter = PlaylistBottomSheetAdapter { playlist ->
            onPlaylistSelected(playlist)
        }
        playlistsRecycler.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        playlistsRecycler.adapter = playlistsAdapter
        observePlaylists()
    }

    private fun observePlaylists() {
        lifecycleScope.launch {
            (applicationContext as App).database.playlistDao().getPlaylists().collectLatest {
                playlistsAdapter.setItems(it)
            }
        }
    }

    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (::bottomSheetBehavior.isInitialized &&
                    bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN
                ) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                } else {
                    finish()
                }
            }
        })
    }

    private fun onPlaylistSelected(playlist: PlaylistEntity) {
        lifecycleScope.launch {
            val isAdded = withContext(Dispatchers.IO) {
                (applicationContext as App).database.playlistDao()
                    .addTrackToPlaylist(PlaylistTrackCrossRef(playlist.id, currentTrack.trackId))
            }
            val message = if (isAdded) {
                getString(R.string.playlist_track_added, playlist.name)
            } else {
                getString(R.string.playlist_track_already_added, playlist.name)
            }
            android.widget.Toast.makeText(this@AudioPlayerActivity, message, android.widget.Toast.LENGTH_SHORT).show()
            if (isAdded) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun checkFavoriteState(trackId: Long) {
        lifecycleScope.launch {
            isFavorite = withContext(Dispatchers.IO) {
                (applicationContext as App).database.trackDao().isFavorite(trackId)
            }
            renderLikeButton()
        }
    }

    private fun onLikeClicked() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val dao = (applicationContext as App).database.trackDao()
                if (isFavorite) {
                    dao.getTrackById(currentTrack.trackId)?.let { dao.deleteTrack(it) }
                } else {
                    dao.insertTrack(currentTrack.toEntity())
                }
            }
            isFavorite = !isFavorite
            renderLikeButton()
        }
    }

    private fun renderLikeButton() {
        if (!::likeButton.isInitialized) return
        likeButton.setImageResource(if (isFavorite) R.drawable.ic_like_filled else R.drawable.ic_like)
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) {
            playButton.isEnabled = false
            playButton.setImageResource(R.drawable.ic_play)
            return
        }

        try {
            mediaPlayer.setDataSource(previewUrl)
            mediaPlayer.setOnPreparedListener {
                playButton.isEnabled = true
                playerState = STATE_PREPARED
            }
            mediaPlayer.setOnCompletionListener {
                playerState = STATE_PREPARED
                playButton.setImageResource(R.drawable.ic_play)
                currentTimeValue.text = getString(R.string.audio_player_current_time_default)
                stopProgressTimer()
            }
            mediaPlayer.prepareAsync()
        } catch (_: IOException) {
            playButton.isEnabled = false
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playButton.setImageResource(R.drawable.ic_pause)
        playerState = STATE_PLAYING
        startProgressTimer()
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        playButton.setImageResource(R.drawable.ic_play)
        playerState = STATE_PAUSED
        stopProgressTimer()
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startProgressTimer() {
        playerHandler.removeCallbacks(progressRunnable)
        playerHandler.post(progressRunnable)
    }

    private fun stopProgressTimer() {
        playerHandler.removeCallbacks(progressRunnable)
    }

    override fun onPause() {
        super.onPause()
        if (playerState == STATE_PLAYING) {
            pausePlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopProgressTimer()
        mediaPlayer.release()
    }

    companion object {
        private const val TRACK_JSON_KEY = "track_json"
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_REFRESH_DELAY_MILLIS = 300L

        fun createIntent(context: Context, track: Track): Intent {
            val trackJson = Gson().toJson(track)
            return Intent(context, AudioPlayerActivity::class.java)
                .putExtra(TRACK_JSON_KEY, trackJson)
        }
    }
}
