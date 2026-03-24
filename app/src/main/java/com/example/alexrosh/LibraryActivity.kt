package com.example.alexrosh

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryActivity : AppCompatActivity() {

    private lateinit var tabs: TabLayout
    private lateinit var favoriteTracksRecyclerView: RecyclerView
    private lateinit var emptyFavoriteTracksPlaceholder: LinearLayout
    private lateinit var favoriteTracksAdapter: TrackAdapter
    private lateinit var playlistsRecyclerView: RecyclerView
    private lateinit var playlistsContent: LinearLayout
    private lateinit var emptyPlaylistsPlaceholder: LinearLayout
    private lateinit var playlistsAdapter: PlaylistGridAdapter
    private lateinit var newPlaylistButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)

        initViews()
        setupRecycler()
        setupTabs()
        observePlaylists()
    }

    override fun onResume() {
        super.onResume()
        loadFavoriteTracks()
    }

    private fun initViews() {
        val backButton = findViewById<ImageView>(R.id.back_button)
        tabs = findViewById(R.id.library_tabs)
        favoriteTracksRecyclerView = findViewById(R.id.favorite_tracks_recycler_view)
        emptyFavoriteTracksPlaceholder = findViewById(R.id.empty_favorite_tracks_placeholder)
        playlistsRecyclerView = findViewById(R.id.playlists_recycler_view)
        playlistsContent = findViewById(R.id.playlists_content)
        emptyPlaylistsPlaceholder = findViewById(R.id.empty_playlists_placeholder)
        newPlaylistButton = findViewById(R.id.new_playlist_button)
        backButton.setOnClickListener { finish() }
        newPlaylistButton.setOnClickListener {
            startActivity(NewPlaylistActivity.createIntent(this))
        }
    }

    private fun setupRecycler() {
        favoriteTracksAdapter = TrackAdapter { track ->
            startActivity(AudioPlayerActivity.createIntent(this, track))
        }
        playlistsAdapter = PlaylistGridAdapter { }
        favoriteTracksRecyclerView.layoutManager = LinearLayoutManager(this)
        favoriteTracksRecyclerView.adapter = favoriteTracksAdapter
        playlistsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        playlistsRecyclerView.adapter = playlistsAdapter
    }

    private fun setupTabs() {
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == TAB_FAVORITES) {
                    showFavoritesTab()
                } else {
                    showPlaylistsTab()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
        showFavoritesTab()
    }

    private fun observePlaylists() {
        lifecycleScope.launch {
            (applicationContext as App).database.playlistDao().getPlaylists().collectLatest { playlists ->
                playlistsAdapter.setItems(playlists)
                val isEmpty = playlists.isEmpty()
                playlistsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
                if (tabs.selectedTabPosition == TAB_PLAYLISTS) {
                    emptyPlaylistsPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun loadFavoriteTracks() {
        lifecycleScope.launch {
            val favoriteTracks = withContext(Dispatchers.IO) {
                (applicationContext as App).database.trackDao().getAllTracks().map { it.toTrack() }
            }

            favoriteTracksAdapter.tracks.clear()
            favoriteTracksAdapter.tracks.addAll(favoriteTracks)
            favoriteTracksAdapter.notifyDataSetChanged()

            if (tabs.selectedTabPosition == TAB_FAVORITES) {
                if (favoriteTracks.isEmpty()) {
                    favoriteTracksRecyclerView.visibility = View.GONE
                    emptyFavoriteTracksPlaceholder.visibility = View.VISIBLE
                } else {
                    favoriteTracksRecyclerView.visibility = View.VISIBLE
                    emptyFavoriteTracksPlaceholder.visibility = View.GONE
                }
            }
        }
    }

    private fun showFavoritesTab() {
        playlistsContent.visibility = View.GONE
        emptyPlaylistsPlaceholder.visibility = View.GONE
        favoriteTracksRecyclerView.visibility =
            if (favoriteTracksAdapter.itemCount == 0) View.GONE else View.VISIBLE
        emptyFavoriteTracksPlaceholder.visibility =
            if (favoriteTracksAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun showPlaylistsTab() {
        playlistsContent.visibility = View.VISIBLE
        favoriteTracksRecyclerView.visibility = View.GONE
        emptyFavoriteTracksPlaceholder.visibility = View.GONE
        val isEmpty = playlistsAdapter.itemCount == 0
        playlistsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyPlaylistsPlaceholder.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAB_FAVORITES = 0
        private const val TAB_PLAYLISTS = 1
    }
}