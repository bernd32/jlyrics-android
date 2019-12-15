/*
 * Copyright 2019 bernd32
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bernd32.jlyrics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.async.AsyncTaskListener;
import com.bernd32.jlyrics.async.PostDataAsyncTask;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class FuriganaActivity extends AppCompatActivity {

    private static final String TAG = "FuriganaActivity";
    private ProgressBar progressBar;
    private ExtendedFloatingActionButton cancelFAB;
    private PostDataAsyncTask task;
    private String savedHtmlString = "";
    private boolean darkTheme;
    private float fontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_furigana);
        progressBar = findViewById(R.id.progress_bar);
        cancelFAB = findViewById(R.id.load_cancel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        String lyrics = intent.getStringExtra("lyrics_text");
        darkTheme = intent.getBooleanExtra("theme", false);
        fontSize = intent.getFloatExtra("font_size", 20);
        // Load lyrics from savedInstanceState, so we don't have to call startAsyncTask
        // if activity is stopped or destroyed
        if (savedInstanceState == null) {
            startAsyncTask(lyrics);
        } else {
            savedHtmlString = savedInstanceState.getString("saved_lyrics");
            startWebView(savedHtmlString);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        if (!savedHtmlString.isEmpty()) {
            outState.putString("saved_lyrics", savedHtmlString);
        }
        super.onSaveInstanceState(outState);
    }

    private void startAsyncTask(String textToConvert) {
        Log.d(TAG, "startAsyncTask: started");
        task = new PostDataAsyncTask(new AsyncTaskListener<String>() {

            @Override
            public void onPreTask() {
                Log.d(TAG, "onPreTask: started");
                progressBar.setVisibility(View.VISIBLE);
                cancelFAB.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostTask(String htmlString) {
                Log.d(TAG, "onPostTask: started");
                // Save a string from AsyncTask to the local variable
                savedHtmlString = htmlString;
                progressBar.setVisibility(View.GONE);
                cancelFAB.setVisibility(View.GONE);
                startWebView(htmlString);
            }

            @Override
            public void onFailure(Exception e, int statusCode) {
                Toast.makeText(FuriganaActivity.this,
                        getString(R.string.connection_failed),
                        Toast.LENGTH_LONG).show();
                finish();
                // TODO: 24.11.2019 log Exception e and int statusCode
                Log.d(TAG, "onFailure: " + e.toString());
            }
        });
        task.execute(textToConvert);
    }

    public void onLoadCancel(View view) {
        if (task != null)
            task.cancel(true);
        finish();
    }

    private void startWebView(String htmlString) {
        WebView webView = findViewById(R.id.webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setTextZoom(120);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        String html = setUpHtmlString(htmlString, (int)fontSize, darkTheme);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
        webView.loadData(html,"text/html", "UTF-8");
    }

    private String setUpHtmlString(String htmlString, int fontSize, boolean darkTheme) {
        String bgColor = darkTheme ? "black":"white";
        String fontColor = darkTheme ? "white":"black";
        String html = String.format("<html><head><style>"
                + "span {color:%s}"
                + "</style></head><body style=\"background-color:%s\">"
                + "%s"
                + "</body></html>",fontColor, bgColor, htmlString);
        Log.d(TAG, "setUpHtmlString: "+html);
        return html;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: started");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (task != null)
            task.cancel(true);
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
