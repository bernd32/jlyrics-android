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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.async.AsyncTaskListener;
import com.bernd32.jlyrics.async.GetDataAsyncTask;
import com.bernd32.jlyrics.async.RomanizeAsyncTask;
import com.bernd32.jlyrics.database.FavLyrics;
import com.bernd32.jlyrics.database.LyricsViewModel;
import com.bernd32.jlyrics.utils.PreferencesManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import org.jsoup.nodes.Document;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.bernd32.jlyrics.utils.HelperClass.addNewLines;

public class ShowLyricsActivity extends AppCompatActivity implements ChangeFontDialogFragment.OnDialogButtonClick {

    private static final String TAG = "ShowLyricsActivity";
    private ProgressBar progressBar;
    public TextView lyricsTV;
    private String lyrics = "";
    private String romanizedLyrics = "";
    private String title;
    private String songUrl;
    private String cardDesc;
    private String cardTitle;
    private String imgUrl;
    private LyricsViewModel lyricsViewModel;
    private boolean hasLyrics;
    private ExtendedFloatingActionButton cancelFAB;
    private GetDataAsyncTask task;
    private boolean romanized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: activated");
        super.onCreate(savedInstanceState);
        PreferencesManager.initializeInstance(this);
        setContentView(R.layout.activity_show_lyrics);
        lyricsTV = findViewById(R.id.lyrics);
        progressBar = findViewById(R.id.progress_bar);
        cancelFAB = findViewById(R.id.load_cancel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        // For saving lyrics to favs
        lyricsViewModel = new ViewModelProvider(this).get(LyricsViewModel.class);
        loadIntents();
        try {
            isLyricsInDB(songUrl);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        // Load lyrics from savedInstanceState, so we don't have to call startAsyncTask
        // if activity is stopped or destroyed
        loadLyrics(savedInstanceState);
    }

    private void loadLyrics(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            startAsyncTask(songUrl);
        } else {
            romanized = savedInstanceState.getBoolean("romanized");
            lyrics = savedInstanceState.getString("saved_lyrics");
            romanizedLyrics = savedInstanceState.getString("saved_romaji_lyrics");
            if (!romanized) {
                lyricsTV.setText(lyrics);
            } else {
                lyricsTV.setText(romanizedLyrics);
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: started");
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: start");
        outState.putString("saved_lyrics", lyrics);
        outState.putString("saved_romaji_lyrics", romanizedLyrics);
        outState.putBoolean("romanized", romanized);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lyrics, menu);
        MenuItem menuShareItem = menu.findItem(R.id.toolbar_share);
        ShareActionProvider shareActionProvider = (ShareActionProvider)
                MenuItemCompat.getActionProvider(menuShareItem);
        // Get saved data and set values
        MenuItem darkThemeItem = menu.findItem(R.id.dark_theme);
        PreferencesManager pm = PreferencesManager.getInstance();
        darkThemeItem.setChecked(pm.getDarkThemeSelected());
        int fontSize = pm.getFontSize();
        lyricsTV.setTextSize((float) fontSize);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.romanize:
                convertLyrics();
                return true;
            case R.id.toolbar_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, lyrics);
                sendIntent.setType("text/plain");
                Intent shareIntent = Intent.createChooser(sendIntent, title);
                startActivity(shareIntent);
                return true;
            case R.id.toolbar_furigana:
                openFuriganaActivity();
                return true;
            case R.id.fav_add_remove:
                saveLyrics();
                return true;
            case R.id.dark_theme:
                item.setChecked(!item.isChecked());
                PreferencesManager pm = PreferencesManager.getInstance();
                pm.setDarkThemeSelected(item.isChecked());
                if (item.isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            case R.id.font_size:
                showFontSizeDialog();
                return true;
            case R.id.quit:
                this.finishAffinity();
                return true;
            case R.id.copy:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.lyrics), lyrics);
                assert clipboard != null;
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void convertLyrics() {
        if (!romanized) {
            RomanizeAsyncTask romanizeTask = new RomanizeAsyncTask(new AsyncTaskListener<String>() {
                @Override
                public void onPreTask() {
                    lyricsTV.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    cancelFAB.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPostTask(String string) {
                    progressBar.setVisibility(View.GONE);
                    cancelFAB.setVisibility(View.GONE);
                    ShowLyricsActivity.this.romanizedLyrics = string;
                    lyricsTV.setText(string);
                    romanized = true;
                }

                @Override
                public void onFailure(Exception e, int statusCode) {
                    Toast.makeText(ShowLyricsActivity.this,
                            getString(R.string.connection_failed),
                            Toast.LENGTH_LONG).show();
                    finish();
                    // TODO: 24.11.2019 log Exception e and int statusCode
                    Log.d(TAG, "onFailure: " + e.toString());
                }
            });
            romanizeTask.execute(lyrics);
        } else {
            // If the text is already converted to romaji then show original Japanese text
            lyricsTV.setText(lyrics);
            romanized = false;
        }
    }

    private void openFuriganaActivity() {
        PreferencesManager pm = PreferencesManager.getInstance();
        // Create an intent and open a new activity
        Intent intent = new Intent(this, FuriganaActivity.class);
        intent.putExtra("lyrics_text", lyrics);
        intent.putExtra("font_size", lyricsTV.getTextSize());
        intent.putExtra("theme", pm.getDarkThemeSelected());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onOkClicked(DialogFragment dialog) {
        Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancelClicked(DialogFragment dialog) {
    }

    @Override
    public void changeFontSize(int i) {
        // Get data from DialogFragment interface,
        // change font size of lyric text and save the value
        lyricsTV.setTextSize((float) i);
        PreferencesManager pm = PreferencesManager.getInstance();
        pm.setFontSize(i);
    }

    @Override
    public boolean onSupportNavigateUp(){
        if (task != null)
            task.cancel(true);
        finish();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (hasLyrics)
            menu.findItem(R.id.fav_add_remove).setTitle(getString(R.string.favs_remove));
        else
            menu.findItem(R.id.fav_add_remove).setTitle(getString(R.string.favs_add));

        if (romanized) {
            menu.findItem(R.id.romanize).setTitle(getString(R.string.show_original));
        } else {
            menu.findItem(R.id.romanize).setTitle(getString(R.string.romanize));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public void onLoadCancel(View view) {
        if (task != null)
            task.cancel(true);
        finish();
    }

    private void showFontSizeDialog() {
        DialogFragment changeFontAlert = new ChangeFontDialogFragment();
        changeFontAlert.show(getSupportFragmentManager(), "changeFontAlert");
    }

    private void loadIntents() {
        if (getIntent().hasExtra("song_url")){
            Log.d(TAG, "getIncomingIntent: found intent extras.");
            songUrl = getIntent().getStringExtra("song_url");
        }
        imgUrl = getIntent().getStringExtra("img_url");
        cardTitle = getIntent().getStringExtra("title");
        cardDesc = getIntent().getStringExtra("description");
    }

    private void isLyricsInDB(String songUrl) throws ExecutionException, InterruptedException {
        this.hasLyrics = lyricsViewModel.isLyricsExists(songUrl);
    }

    private void saveLyrics() {
        if (!hasLyrics) {
            long timestamp = System.currentTimeMillis();
            FavLyrics savedLyrics = new FavLyrics(cardTitle, cardDesc, songUrl, imgUrl, timestamp);
            lyricsViewModel.insert(savedLyrics);
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            this.hasLyrics = true;
        } else {
            // Remove lyrics
            lyricsViewModel.deleteLyricsByUrl(songUrl);
            Toast.makeText(this, getString(R.string.removed), Toast.LENGTH_SHORT).show();
            this.hasLyrics = false;
            finish();
        }
    }

    private void startAsyncTask(String url) {
        Log.d(TAG, "startAsyncTask: " + url);
        task = new GetDataAsyncTask(new AsyncTaskListener<Document>() {
            @Override
            public void onPreTask() {
                Log.d(TAG, "onPreTask: started");
                // show progressbar only in initial loading
                progressBar.setVisibility(View.VISIBLE);
                cancelFAB.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPostTask(Document doc) {
                Log.d(TAG, "onPostTask: started");
                progressBar.setVisibility(View.GONE);
                cancelFAB.setVisibility(View.GONE);
                showLyrics(doc);
            }

            @Override
            public void onFailure(Exception e, int statusCode) {
                Toast.makeText(ShowLyricsActivity.this,
                        getString(R.string.connection_failed),
                        Toast.LENGTH_LONG).show();
                finish();
                // TODO: 24.11.2019 log Exception e and int statusCode
            }
        });
        task.execute(url);
    }

    private void showLyrics(Document doc) {
        Log.d(TAG, "showLyrics: activated");
        lyrics = doc.select("#Lyric").first().html();
        String songName = doc.select("#mnb > div.cap > h2")
                .text()
                .replace(" 歌詞", "");
        title =  String.format(Locale.getDefault(), "「%s」\n\n\n", songName);
        ShowLyricsActivity.this.setTitle(title);
        lyrics = addNewLines(lyrics);
        lyrics = lyrics.replace("(△くり返し)", getText(R.string.repeat));
        lyrics = lyrics.replace("(※くり返し)", getText(R.string.repeat2));
        SpannableString songTitle = new SpannableString(title);
        songTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        lyricsTV.setText(songTitle);
        lyricsTV.append(lyrics);
        Log.d(TAG, "Current artist: " + cardDesc);
        Log.d(TAG, "Current song: " + cardTitle);
    }
}
