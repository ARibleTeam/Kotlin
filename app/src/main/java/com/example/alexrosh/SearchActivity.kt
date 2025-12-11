package com.example.alexrosh

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SearchActivity : AppCompatActivity() {

    private var currentSearchQuery: String = ""

    private lateinit var searchEditText: EditText

    // Результаты поиска
    private val mockTracks = ArrayList<Track>()
    private lateinit var resultsAdapter: TrackAdapter
    private lateinit var resultsRecyclerView: RecyclerView

    // История поиска
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyView: LinearLayout
    private lateinit var clearHistoryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // Инициализация View
        searchEditText = findViewById(R.id.search_edit_text)
        val clearIcon = findViewById<ImageView>(R.id.clear_icon)
        val backButton = findViewById<ImageView>(R.id.back_button)
        historyView = findViewById(R.id.search_history_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        resultsRecyclerView = findViewById(R.id.track_recycler_view)
        historyRecyclerView = findViewById(R.id.history_recycler_view)

        // --- Инициализация истории ---
        val sharedPrefs = getSharedPreferences(SEARCH_HISTORY_PREFS, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)
        historyAdapter = TrackAdapter { track ->
            Toast.makeText(this, "Откроется плеер для ${track.trackName}", Toast.LENGTH_SHORT).show()
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyAdapter.tracks = searchHistory.read()

        // --- Инициализация результатов поиска ---
        resultsAdapter = TrackAdapter { track ->
            searchHistory.addTrack(track)
            historyAdapter.tracks = searchHistory.read()
            historyAdapter.notifyDataSetChanged()
        }
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = resultsAdapter
        fillMockTracks()
        resultsAdapter.tracks = mockTracks

        // --- Слушатели ---
        backButton.setOnClickListener { finish() }

        clearIcon.setOnClickListener {
            searchEditText.setText("")
            hideKeyboard(it)
        }

        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
            historyAdapter.tracks.clear()
            historyAdapter.notifyDataSetChanged()
            hideHistoryView()
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            showOrHideHistory(hasFocus, searchEditText.text)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                clearIcon.visibility = clearButtonVisibility(s)
                showOrHideHistory(searchEditText.hasFocus(), s)
                resultsRecyclerView.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)

        // Начальное состояние
        showOrHideHistory(searchEditText.hasFocus(), searchEditText.text)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY, currentSearchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentSearchQuery = savedInstanceState.getString(SEARCH_QUERY, "")
        searchEditText.setText(currentSearchQuery)
    }

    private fun showOrHideHistory(hasFocus: Boolean, text: CharSequence?) {
        val shouldShowHistory = hasFocus && text.isNullOrEmpty() && historyAdapter.tracks.isNotEmpty()
        historyView.visibility = if (shouldShowHistory) View.VISIBLE else View.GONE
    }

    private fun showHistoryView() {
        resultsRecyclerView.visibility = View.GONE
        historyView.visibility = View.VISIBLE
    }

    private fun hideHistoryView() {
        historyView.visibility = View.GONE
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun fillMockTracks() {
        mockTracks.clear()
        mockTracks.add(Track(1L, "Smells Like Teen Spirit", "Nirvana", 301000L, "https://is5-ssl.mzstatic.com/image/thumb/Music115/v4/7b/58/c2/7b58c21a-2b51-2bb2-e59a-9bb9b96ad8c3/00602567924166.rgb.jpg/100x100bb.jpg"))
        mockTracks.add(Track(2L, "Billie Jean", "Michael Jackson", 275000L, "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/3d/9d/38/3d9d3811-71f0-3a0e-1ada-3004e56ff852/827969428726.jpg/100x100bb.jpg"))
        mockTracks.add(Track(3L, "Stayin' Alive", "Bee Gees", 250000L, "https://is4-ssl.mzstatic.com/image/thumb/Music115/v4/1f/80/1f/1f801fc1-8c0f-ea3e-d3e5-387c6619619e/16UMGIM86640.rgb.jpg/100x100bb.jpg"))
        mockTracks.add(Track(4L, "Whole Lotta Love", "Led Zeppelin", 333000L, "https://is2-ssl.mzstatic.com/image/thumb/Music62/v4/7e/17/e3/7e17e33f-2efa-2a36-e916-7f808576cf6b/mzm.fyigqcbs.jpg/100x100bb.jpg"))
        mockTracks.add(Track(5L, "Sweet Child O'Mine", "Guns N' Roses", 303000L, "https://is5-ssl.mzstatic.com/image/thumb/Music125/v4/a0/4d/c4/a04dc484-03cc-02aa-fa82-5334fcb4bc16/18UMGIM24878.rgb.jpg/100x100bb.jpg"))
    }

    companion object {
        private const val SEARCH_QUERY = "SEARCH_QUERY"
    }
}