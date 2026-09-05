package ru.otvykaniye.tracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class MainActivity : Activity() {

    companion object {
        const val IMPORT_REQUEST = 741
    }

    private var webView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = 0xFF0A1310.toInt()
        window.navigationBarColor = 0xFF060B09.toInt()

        webView = WebView(this).apply {
            setBackgroundColor(0xFF0A1310.toInt())
            fitsSystemWindows = true
            setPadding(0, 0, 0, 0)
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                builtInZoomControls = false
                displayZoomControls = false
                textZoom = 100
            }

            addJavascriptInterface(TrackerBridge(this@MainActivity), "AndroidBridge")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    view.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();", null)
                    view.postDelayed({
                        view.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();", null)
                    }, 350)
                }
            }
            webChromeClient = WebChromeClient()
            overScrollMode = View.OVER_SCROLL_NEVER
        }

        setContentView(webView)
        webView?.loadUrl("file:///android_asset/index.html")
    }

    override fun onResume() {
        super.onResume()
        webView?.postDelayed({
            webView?.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();", null)
        }, 250)
    }

    override fun onPause() {
        super.onPause()
        webView?.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();", null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != IMPORT_REQUEST || resultCode != RESULT_OK || data == null) return

        val uri: Uri? = data.data
        if (uri == null || webView == null) return

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { reader ->
                    val json = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        json.append(line).append('\n')
                    }
                    val escaped = JSONObject.quote(json.toString())
                    webView?.evaluateJavascript(
                        "window.__importFromAndroid && window.__importFromAndroid($escaped);",
                        null
                    )
                }
            }
        } catch (ignored: Exception) {
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
