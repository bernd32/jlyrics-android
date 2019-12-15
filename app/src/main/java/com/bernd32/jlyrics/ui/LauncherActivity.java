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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Group;

import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.utils.PreferencesManager;

public class LauncherActivity extends AppCompatActivity {

    private static final String TAG = "LauncherActivity";
    public static final String SEARCH_QUERY_SONG = "com.bernd32.jlyrics.SEARCH_QUERY_SONG";
    public static final String SPINNER_LIST = "com.bernd32.jlyrics.SPINNER_LIST";
    public static final String ARTIST_INPUT = "com.bernd32.jlyrics.ARTIST_INPUT";
    public static final String SONG_INPUT = "com.bernd32.jlyrics.SONG_INPUT";
    public static final String LYRICS_INPUT = "com.bernd32.jlyrics.LYRICS_INPUT";
    public static final String ARTIST_SPINNER = "com.bernd32.jlyrics.ARTIST_SPINNER";
    public static final String SONG_SPINNER = "com.bernd32.jlyrics.SONG_SPINNER";
    public static final String LYRICS_SPINNER = "com.bernd32.jlyrics.LYRICS_SPINNER";
    public static final String IGNORE_MAIN_SEARCH_INPUT = "com.bernd32.jlyrics.IGNORE_MAIN_SEARCH_INPUT";

    private EditText mainSearchInput;
    private EditText songInput;
    private EditText artistInput;
    private EditText lyricsInput;
    private Spinner songSpinner;
    private Spinner artistSpinner;
    private Spinner lyricsSpinner;
    private Spinner spinner;
    private Button detailedSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainSearchInput = findViewById(R.id.search_input);
        spinner = findViewById(R.id.spinner);
        songSpinner =  findViewById(R.id.song_spinner);
        artistSpinner =  findViewById(R.id.artist_spinner);
        lyricsSpinner =  findViewById(R.id.lyrics_spinner);
        songInput = findViewById(R.id.song_input);
        artistInput = findViewById(R.id.artist_input);
        lyricsInput = findViewById(R.id.lyrics_input);
        detailedSearchButton = findViewById(R.id.detailed_search_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_items, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter_d = ArrayAdapter.createFromResource(this,
                R.array.spinner_search_options, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> adapter_l = ArrayAdapter.createFromResource(this,
                R.array.lyrics_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_d.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter_l.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        songSpinner.setAdapter(adapter_d);
        artistSpinner.setAdapter(adapter_d);
        lyricsSpinner.setAdapter(adapter_l);
        // Set default values to the spinners
        songSpinner.setSelection(2);
        artistSpinner.setSelection(2);
        lyricsSpinner.setSelection(1);
        // Load theme settings from PrefManager
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO);
        PreferencesManager.initializeInstance(this);
        PreferencesManager pm = PreferencesManager.getInstance();
        if (pm.getDarkThemeSelected()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Get saved data and set value to the checkable menu item
        MenuItem darkThemeItem = menu.findItem(R.id.dark_theme);
        PreferencesManager.initializeInstance(this);
        PreferencesManager pm = PreferencesManager.getInstance();
        darkThemeItem.setChecked(pm.getDarkThemeSelected());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_show_favs:
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            case R.id.quit:
                this.finishAffinity();
                return true;
            case R.id.contact:
                sendMail(getString(R.string.email_address), getString(R.string.email_subject_question));
            case R.id.dark_theme:
                item.setChecked(!item.isChecked());
                // Save to value
                PreferencesManager.initializeInstance(this);
                PreferencesManager pm = PreferencesManager.getInstance();
                pm.setDarkThemeSelected(item.isChecked());
                if (item.isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendMail(String email, String subject) {
        String[] email_address = new String[] {email};
        Intent mailto = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        mailto.putExtra(Intent.EXTRA_EMAIL, email_address);
        mailto.putExtra(Intent.EXTRA_SUBJECT, subject);
        startActivity(Intent.createChooser(mailto, "Send E-mail"));
    }

    public void onLoadLyrics(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        boolean ignoreMainSearchInput = true; 
        String mainSearchText = mainSearchInput.getText().toString().trim();
        int spinnerPos = spinner.getSelectedItemPosition();
        String artistText = artistInput.getText().toString().trim();
        String songText = songInput.getText().toString().trim();
        String lyricsText = lyricsInput.getText().toString().trim();
        int artistSpinnerPos = artistSpinner.getSelectedItemPosition();
        int songSpinnerPos = songSpinner.getSelectedItemPosition();
        int lyricsSpinnerPos = lyricsSpinner.getSelectedItemPosition();
        if (artistText.isEmpty() && songText.isEmpty() && lyricsText.isEmpty()) {
            ignoreMainSearchInput = false;
        }
        if (!mainSearchText.isEmpty() && !ignoreMainSearchInput) {
            Log.d(TAG, "onLoadLyrics: user didn't use detailed search");
            intent.putExtra(SEARCH_QUERY_SONG, mainSearchText);
            intent.putExtra(SPINNER_LIST, spinnerPos);
        }
        if (ignoreMainSearchInput){
            Log.d(TAG, "onLoadLyrics: user used detailed search");
            intent.putExtra(ARTIST_INPUT, artistText);
            intent.putExtra(SONG_INPUT, songText);
            intent.putExtra(LYRICS_INPUT, lyricsText);
            intent.putExtra(ARTIST_SPINNER, artistSpinnerPos);
            intent.putExtra(SONG_SPINNER, songSpinnerPos);
            intent.putExtra(LYRICS_SPINNER, lyricsSpinnerPos);
        }
        if (intent.resolveActivity(getPackageManager()) != null &&
                (!mainSearchText.isEmpty() || ignoreMainSearchInput)) {
            intent.putExtra(IGNORE_MAIN_SEARCH_INPUT, ignoreMainSearchInput);
            startActivity(intent);
        }
    }

    public void onDetailedSearch(View view) {
        Group group = findViewById(R.id.group);
        if (group.getVisibility() == View.GONE) {
            // Hide keyboard
            view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                assert imm != null;
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            group.setVisibility(View.VISIBLE);
            detailedSearchButton.setText(getString(R.string.detailed_search_hide));
        }
        else {
            group.setVisibility(View.GONE);
            detailedSearchButton.setText(getString(R.string.detailed_search_show));
        }
    }
}
