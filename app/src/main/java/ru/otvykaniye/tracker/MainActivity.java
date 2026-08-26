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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int CREATE_BACKUP = 4101;
    private static final int OPEN_BACKUP = 4102;
    private WebView webView;
    private String pendingExportJson;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(0xFF0F1E1A); getWindow().setNavigationBarColor(0xFF0B1613);
        webView = new WebView(this); webView.setBackgroundColor(0xFF0F1E1A); webView.setFitsSystemWindows(true); webView.setPadding(0,0,0,0);
        WebSettings settings = webView.getSettings(); settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true); settings.setDatabaseEnabled(true); settings.setAllowFileAccess(true); settings.setAllowContentAccess(true); settings.setBuiltInZoomControls(false); settings.setDisplayZoomControls(false); settings.setTextZoom(100);
        webView.addJavascriptInterface(new TrackerBridge(this), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView view,String url){ view.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();",null); view.postDelayed(()->view.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();",null),350); }});
        webView.setWebChromeClient(new WebChromeClient()); webView.setOverScrollMode(View.OVER_SCROLL_NEVER); setContentView(webView); webView.loadUrl("file:///android_asset/index.html");
    }
    public void exportBackup(String json){ Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("application/json"); intent.putExtra(Intent.EXTRA_TITLE,"trackless-backup.json"); startActivityForResult(intent,CREATE_BACKUP); pendingExportJson=json; }
    public void importBackup(){ Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT); intent.addCategory(Intent.CATEGORY_OPENABLE); intent.setType("application/json"); intent.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/json","text/json","text/plain"}); startActivityForResult(intent,OPEN_BACKUP); }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){ super.onActivityResult(requestCode,resultCode,data); if(resultCode!=RESULT_OK||data==null||data.getData()==null)return; Uri uri=data.getData(); try{ if(requestCode==CREATE_BACKUP){ if(pendingExportJson==null)return; try(OutputStream out=getContentResolver().openOutputStream(uri)){ if(out==null)throw new Exception("Cannot open output"); out.write(pendingExportJson.getBytes(StandardCharsets.UTF_8)); out.flush(); } pendingExportJson=null; }else if(requestCode==OPEN_BACKUP){ StringBuilder result=new StringBuilder(); try(InputStream in=getContentResolver().openInputStream(uri); BufferedReader reader=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8))){ String line; while((line=reader.readLine())!=null)result.append(line).append('\n'); } String escaped=org.json.JSONObject.quote(result.toString()); webView.evaluateJavascript("window.__importBackupFromAndroid && window.__importBackupFromAndroid("+escaped+");",null); } }catch(Exception ignored){ webView.evaluateJavascript("window.__backupErrorFromAndroid && window.__backupErrorFromAndroid();",null); }}
    @Override protected void onResume(){ super.onResume(); if(webView!=null)webView.postDelayed(()->webView.evaluateJavascript("window.__syncFromAndroid && window.__syncFromAndroid();",null),250); }
    @Override protected void onPause(){ super.onPause(); if(webView!=null)webView.evaluateJavascript("window.__saveToAndroid && window.__saveToAndroid();",null); }
    @Override public void onBackPressed(){ if(webView!=null&&webView.canGoBack())webView.goBack(); else super.onBackPressed(); }
}
