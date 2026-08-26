package ru.otvykaniye.tracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    public static final int IMPORT_REQUEST = 741;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep the WebView in the normal content area so the header does not
        // collide with the Android system bars on modern Android versions.
        getWindow().setStatusBarColor(0xFF0F1E1A);
        getWindow().setNavigationBarColor(0xFF0B1613);

        webView = new WebView(this);
        webView.setBackgroundColor(0xFF0F1E1A);
        webView.setFitsSystemWindows(true);
        webView.setPadding(0, 0, 0, 0);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);

        webView.addJavascriptInterface(new TrackerBridge(this), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();", null);
                view.postDelayed(() ->
                        view.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();", null), 350);
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.postDelayed(() ->
                    webView.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();", null), 250);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();", null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != IMPORT_REQUEST || resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (uri == null || webView == null) return;

        try (InputStream input = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line).append('\n');
            }
            String escaped = org.json.JSONObject.quote(json.toString());
            webView.evaluateJavascript(
                    "window.__importFromAndroid && window.__importFromAndroid(" + escaped + ");",
                    null
            );
        } catch (Exception ignored) {
            // The web UI will keep the current state if the backup cannot be read.
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
