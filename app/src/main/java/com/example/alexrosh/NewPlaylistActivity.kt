package com.example.alexrosh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewPlaylistActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var createButton: MaterialButton
    private lateinit var coverImage: ImageView
    private lateinit var coverHint: View

    private var selectedCoverPath: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@registerForActivityResult
        lifecycleScope.launch {
            val path = withContext(Dispatchers.IO) { saveImageToPrivateStorage(uri) }
            if (path != null) {
                selectedCoverPath = path
                renderCover(path)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_playlist)
        initViews()
        setupListeners()
        setupBackHandler()
    }

    private fun initViews() {
        nameInput = findViewById(R.id.name_input)
        descriptionInput = findViewById(R.id.description_input)
        createButton = findViewById(R.id.create_button)
        coverImage = findViewById(R.id.cover_image)
        coverHint = findViewById(R.id.cover_hint)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener { handleBackPress() }
        findViewById<FrameLayout>(R.id.cover_picker).setOnClickListener {
            imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        nameInput.doAfterTextChanged {
            createButton.isEnabled = !nameInput.text.isNullOrBlank()
        }
        createButton.setOnClickListener { createPlaylist() }
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                this@NewPlaylistActivity.handleBackPress()
            }
        })
    }

    private fun handleBackPress() {
        if (!isDataModified()) {
            finish()
            return
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_playlist_finish_creation_title)
            .setPositiveButton(R.string.new_playlist_finish_creation_positive) { _, _ -> finish() }
            .setNegativeButton(R.string.new_playlist_finish_creation_negative, null)
            .show()
    }

    private fun isDataModified(): Boolean {
        return !nameInput.text.isNullOrBlank() ||
            !descriptionInput.text.isNullOrBlank() ||
            !selectedCoverPath.isNullOrBlank()
    }

    private fun createPlaylist() {
        val name = nameInput.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) return
        val description = descriptionInput.text?.toString()?.trim().orEmpty()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                (applicationContext as App).database.playlistDao().insertPlaylist(
                    PlaylistEntity(
                        name = name,
                        description = description,
                        coverPath = selectedCoverPath
                    )
                )
            }
            Toast.makeText(
                this@NewPlaylistActivity,
                getString(R.string.new_playlist_created, name),
                Toast.LENGTH_SHORT
            ).show()
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    private fun renderCover(path: String) {
        coverHint.visibility = View.GONE
        Glide.with(this)
            .load(File(path))
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .centerCrop()
            .into(coverImage)
    }

    private fun saveImageToPrivateStorage(uri: Uri): String? {
        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
        val coversDir = File(picturesDir, "playlist_covers")
        if (!coversDir.exists()) coversDir.mkdirs()
        val file = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
            return file.absolutePath
        }
        return null
    }

    companion object {
        fun createIntent(context: android.content.Context): Intent {
            return Intent(context, NewPlaylistActivity::class.java)
        }
    }
}
