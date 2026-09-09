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

    // Export intent helper using ActivityResultContracts
    private var exportDataPending: String? = null
    private val exportLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null && exportDataPending != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(exportDataPending!!.toByteArray(StandardCharsets.UTF_8))
                }
                android.widget.Toast.makeText(this, "Exported successfully", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(this, "Export failed", android.widget.Toast.LENGTH_SHORT).show()
            }
            exportDataPending = null
        }
    }

    fun exportData(json: String) {
        exportDataPending = json
        val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US)
        val filename = "trackless_backup_${sdf.format(java.util.Date())}.json"
        exportLauncher.launch(filename)
    }

    // Import intent helper using ActivityResultContracts
    private val importLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                        val json = reader.readText()
                        val state = TracklessState.fromJson(json)
                        viewModel.importState(state)
                        android.widget.Toast.makeText(this, "Data imported successfully", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(this, "Import failed or invalid file", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchImport() {
        importLauncher.launch(arrayOf("application/json", "*/*"))
    }
}

