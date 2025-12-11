package com.example.alexrosh

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private var currentSearchQuery: String = ""
    private var lastSearchQuery: String = ""

    // --- Views ---
    private lateinit var searchEditText: EditText
    private lateinit var resultsRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyView: LinearLayout
    private lateinit var clearHistoryButton: Button
    private lateinit var nothingFoundPlaceholder: LinearLayout
    private lateinit var connectionErrorPlaceholder: LinearLayout
    private lateinit var updateButton: Button
    private lateinit var progressBar: ProgressBar

    // --- Adapters and Data ---
    private val tracks = ArrayList<Track>()
    private lateinit var resultsAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory
    private lateinit var historyAdapter: TrackAdapter

    // --- Network ---
    private val itunesBaseUrl = "https://itunes.apple.com"
    private val retrofit = Retrofit.Builder()
        .baseUrl(itunesBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val itunesService = retrofit.create(ITunesApi::class.java)

    // --- Enum для состояний экрана ---
    private enum class SearchState {
        LOADING,
        CONTENT,
        ERROR,
        EMPTY,
        HISTORY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        initViews()
        setupSearchHistory()
        setupSearchResults()
        setupListeners()

        // Показываем историю, если она есть и поле поиска в фокусе
        if (searchEditText.hasFocus() && searchEditText.text.isEmpty()) {
            showHistoryOrHide()
        }
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

    private fun initViews() {
        searchEditText = findViewById(R.id.search_edit_text)
        historyView = findViewById(R.id.search_history_view)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        resultsRecyclerView = findViewById(R.id.track_recycler_view)
        historyRecyclerView = findViewById(R.id.history_recycler_view)
        nothingFoundPlaceholder = findViewById(R.id.nothing_found_placeholder)
        connectionErrorPlaceholder = findViewById(R.id.connection_error_placeholder)
        updateButton = findViewById(R.id.update_button)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupSearchHistory() {
        val sharedPrefs = getSharedPreferences(SEARCH_HISTORY_PREFS, MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPrefs)
        historyAdapter = TrackAdapter { track ->
            // Тут должен быть переход на плеер, но его тут не будет :))))
            Toast.makeText(this, "Откроется плеер для ${track.trackName}", Toast.LENGTH_SHORT).show()
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter
        historyAdapter.tracks = searchHistory.read()
    }

    private fun setupSearchResults() {
        resultsAdapter = TrackAdapter { track ->
            searchHistory.addTrack(track)
            historyAdapter.tracks = searchHistory.read()
            historyAdapter.notifyDataSetChanged()
            // Тут должен быть переход на плеер, но его тут не будет :))))
        }
        resultsAdapter.tracks = tracks
        resultsRecyclerView.layoutManager = LinearLayoutManager(this)
        resultsRecyclerView.adapter = resultsAdapter
    }

    private fun setupListeners() {
        val backButton = findViewById<ImageView>(R.id.back_button)
        val clearIcon = findViewById<ImageView>(R.id.clear_icon)

        backButton.setOnClickListener { finish() }

        clearIcon.setOnClickListener {
            searchEditText.setText("")
            tracks.clear()
            resultsAdapter.notifyDataSetChanged()
            hideKeyboard(it)
            showHistoryOrHide()
        }

        updateButton.setOnClickListener { searchRequest() }

        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
            historyAdapter.tracks.clear()
            historyAdapter.notifyDataSetChanged()
            historyView.visibility = View.GONE
        }

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) showHistoryOrHide()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequest()
                true
            }
            false
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s.toString()
                clearIcon.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                if (s.isNullOrEmpty() && searchEditText.hasFocus()) {
                    showHistoryOrHide()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)
    }

    private fun searchRequest() {
        if (searchEditText.text.isNotEmpty()) {
            lastSearchQuery = searchEditText.text.toString()
            showState(SearchState.LOADING)

            itunesService.search(lastSearchQuery).enqueue(object : Callback<TracksResponse> {
                override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null && responseBody.results.isNotEmpty()) {
                            tracks.clear()
                            tracks.addAll(responseBody.results)
                            showState(SearchState.CONTENT)
                        } else {
                            showState(SearchState.EMPTY)
                        }
                    } else {
                        showState(SearchState.ERROR)
                    }
                }

                override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                    showState(SearchState.ERROR)
                }
            })
        }
    }

    private fun showHistoryOrHide() {
        if (searchEditText.text.isEmpty() && historyAdapter.tracks.isNotEmpty()) {
            showState(SearchState.HISTORY)
        } else {
            historyView.visibility = View.GONE
        }
    }

    private fun showState(state: SearchState) {
        progressBar.visibility = View.GONE
        resultsRecyclerView.visibility = View.GONE
        nothingFoundPlaceholder.visibility = View.GONE
        connectionErrorPlaceholder.visibility = View.GONE
        historyView.visibility = View.GONE

        when (state) {
            SearchState.LOADING -> progressBar.visibility = View.VISIBLE
            SearchState.CONTENT -> {
                resultsRecyclerView.visibility = View.VISIBLE
                resultsAdapter.notifyDataSetChanged()
            }
            SearchState.ERROR -> connectionErrorPlaceholder.visibility = View.VISIBLE
            SearchState.EMPTY -> nothingFoundPlaceholder.visibility = View.VISIBLE
            SearchState.HISTORY -> historyView.visibility = View.VISIBLE
        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        private const val SEARCH_QUERY = "SEARCH_QUERY"
    }
}