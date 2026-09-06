package ru.otvykaniye.tracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.json.JSONObject
import ru.otvykaniye.tracker.ui.TracklessApp
import ru.otvykaniye.tracker.ui.theme.TracklessTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: TracklessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TracklessTheme {
                TracklessApp(viewModel = viewModel)
            }
        }
    }

    // Export intent helper
    fun exportData(json: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
            putExtra(Intent.EXTRA_TITLE, "trackless-backup.json")
        }
        startActivity(Intent.createChooser(intent, null))
    }

    // Import intent helper (ActivityResult could be cleaner but keeping it simple for now)
    companion object {
        const val IMPORT_REQUEST = 741
    }

    fun launchImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, IMPORT_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMPORT_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                try {
                    contentResolver.openInputStream(uri)?.use { input ->
                        BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                            val jsonBuilder = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                jsonBuilder.append(line).append("\n")
                            }
                            val state = TracklessState.fromJson(jsonBuilder.toString())
                            viewModel.importState(state)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

