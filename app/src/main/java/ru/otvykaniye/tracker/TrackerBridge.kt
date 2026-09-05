package ru.otvykaniye.tracker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.glance.appwidget.updateAll

class TrackerBridge(private val activity: MainActivity) {

    @JavascriptInterface
    fun getStateJson(): String {
        return AppDataStore.getState(activity)
    }

    @JavascriptInterface
    fun saveStateJson(json: String) {
        AppDataStore.saveState(activity, json)
        kotlinx.coroutines.runBlocking {
            SmallTrackerWidget().updateAll(activity)
            WideTrackerWidget().updateAll(activity)
        }
    }

    @JavascriptInterface
    fun exportState(json: String) {
        try {
            val i = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_TEXT, json)
                putExtra(Intent.EXTRA_TITLE, "trackless-backup.json")
            }
            activity.startActivity(Intent.createChooser(i, null))
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(activity, "No sharing app available", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    @JavascriptInterface
    fun importState() {
        try {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/json"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            activity.startActivityForResult(i, MainActivity.IMPORT_REQUEST)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(activity, "No file manager available", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    @JavascriptInterface
    fun haptic(type: String?) {
        activity.runOnUiThread {
            try {
                val v = activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (v == null || !v.hasVibrator()) return@runOnUiThread
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    when {
                        type.equals("tick", ignoreCase = true) || type.equals("light", ignoreCase = true) -> {
                            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                        }
                        type.equals("heavy", ignoreCase = true) || type.equals("success", ignoreCase = true) -> {
                            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                        }
                        else -> {
                            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        }
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val duration = if (type.equals("tick", ignoreCase = true)) 12L else 35L
                    v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    v.vibrate(if (type.equals("tick", ignoreCase = true)) 12L else 35L)
                }
            } catch (ignored: Exception) {}
        }
    }
}
