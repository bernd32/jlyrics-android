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
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bernd32.jlyrics.PaginationListener;
import com.bernd32.jlyrics.Lyric;
import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.adapters.PostRecyclerAdapter;
import com.bernd32.jlyrics.async.GetDataAsyncTask;
import com.bernd32.jlyrics.search.CallbackInterface;
import com.bernd32.jlyrics.search.SearchLyricsViewModel;
import com.bernd32.jlyrics.utils.PreferencesManager;
import com.bernd32.romajihenkan.RomajiHenkan;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.bernd32.jlyrics.PaginationListener.PAGE_START;
import static com.bernd32.jlyrics.utils.HelperClass.urlBuilder;

public class SearchActivity extends AppCompatActivity  {

    private static final String TAG = "SearchActivity";
    public static final String NO_IMG = "no_img";
    @SuppressWarnings("WeakerAccess")
    public RecyclerView mRecyclerView; // must not be private or static
    private ProgressBar progressBar;
    private PostRecyclerAdapter adapter;
    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private String searchInput;
    private int spinnerPos;
    private String artistText;
    private String songText;
    private String lyricsText;
    private int artistSpinnerPos;
    private int songSpinnerPos;
    private int lyricsSpinnerPos;
    private boolean ignoreMainSearch;
    private FloatingActionButton floatingActionButton;
    private ExtendedFloatingActionButton cancelFAB;
    private GetDataAsyncTask task;
    private SearchLyricsViewModel viewModel;
    private int maxPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get intents
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            intentLoader();
        }
        // UI
        setContentView(R.layout.activity_search);
        progressBar = findViewById(R.id.progress_bar);
        mRecyclerView = findViewById(R.id.recyclerv_view);
        floatingActionButton = findViewById(R.id.floating_action_button);
        cancelFAB = findViewById(R.id.load_cancel);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new PostRecyclerAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);
        // initial load
        String url = ignoreMainSearch ?
                urlBuilder(PAGE_START, artistText, artistSpinnerPos, songText,
                        songSpinnerPos, lyricsText, lyricsSpinnerPos) :
                urlBuilder(PAGE_START, spinnerPos, searchInput);
        viewModel = new ViewModelProvider(this).get(SearchLyricsViewModel.class);
        CallbackInterface listener = new CallbackInterface() {

            // Show alert dialog in case when no search results were found
            @Override
            public void showAlertDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
                builder.setTitle(getString(R.string.not_found_title));
                builder.setMessage(getString(R.string.not_found_message));
                builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                    dialog.dismiss();
                    finish();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            // Show number of search result found in the action bar
            @Override
            public void setActivityTitle(int numberOfSongs) {
                SearchActivity.this.setTitle(getString(R.string.title_found) +
                        " " + numberOfSongs);
            }

            // Tell our adapter the search result type we got, so adapter's click listener
            // will know what activity show be opened when we click on a search result
            @Override
            public void setPageType(int pageType) {
                adapter.setPageType(pageType);
            }

            // This is basically the main callback method that takes the search
            // result items from the view model and loads them into the UI via our recycler adapter
            // This callback method also handles the pagination future, and loads new search
            // result pages into our adapter
            @Override
            public void getSearchResults(ArrayList<Lyric> items) {
                maxPage = viewModel.getMaxPage();
                newPageLoader(maxPage, items);
                /*
                 * add scroll listener while user reach in bottom load more will call
                 */
                mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
                    @Override
                    protected void loadMoreItems() {
                        isLoading = true;
                        currentPage++;
                        String url = ignoreMainSearch ?
                                urlBuilder(currentPage, artistText, artistSpinnerPos, songText,
                                        songSpinnerPos, lyricsText, lyricsSpinnerPos) :
                                urlBuilder(currentPage, spinnerPos, searchInput);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        // Load search results from a new page
                        viewModel.newSearchRequest(url);
                    }

                    @Override
                    public boolean isLastPage() {
                        return isLastPage;
                    }

                    @Override
                    public boolean isLoading() {
                        return isLoading;
                    }

                    @Override
                    protected void showFAB() {
                        if (floatingActionButton.getVisibility() != View.VISIBLE) {
                            floatingActionButton.show();
                        }
                    }

                    @Override
                    protected void hideFAB() {
                        if (floatingActionButton.getVisibility() == View.VISIBLE) {
                            floatingActionButton.hide();
                        }
                    }
                });
            }

            // Showing loading animation when user started searching
            @Override
            public void taskStarted() {
                if (currentPage == 1) {
                    progressBar.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    cancelFAB.setVisibility(View.VISIBLE);
                }
            }

            // Hide loading animation and show search results
            @Override
            public void taskFinished() {
                Log.d(TAG, "onPostTask: started");
                if (currentPage == 1) {
                    progressBar.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    cancelFAB.setVisibility(View.GONE);
                }
            }

            // Shows a toast when we got an exception and returns back to main activity
            @Override
            public void taskFailed(Exception e, int statusCode) {
                Toast.makeText(SearchActivity.this,
                        getString(R.string.connection_failed),
                        Toast.LENGTH_LONG).show();
                finish();
                // TODO: 24.11.2019 log Exception e and int statusCode
                Log.e(TAG, "onFailure: " + e.toString());
            }
        };

        floatingActionButton.setOnClickListener(view -> {
            mRecyclerView.smoothScrollToPosition(0);
        });

        viewModel.addListener(listener);
        // Send a request to parse a search results from an URL and get results back
        // to activity via callback method getSearchResults()
        viewModel.newSearchRequest(url);
    }

    private void newPageLoader(int maxPage, ArrayList<Lyric> items) {
        if (currentPage != PAGE_START)  {
            mRecyclerView.post(() -> adapter.removeLoading());
        }
        mRecyclerView.post(() -> adapter.addItems(items));
        // check whether is last page or not
        if (currentPage < maxPage) {
            mRecyclerView.post(() -> adapter.addLoading());
        } else {
            isLastPage = true;
        }
        isLoading = false;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: activated");
        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                // Open favorites activity
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            case R.id.quit:
                this.finishAffinity();
                return true;
            case R.id.dark_theme:
                // Switch color theme of the app (light/dark)
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

    private void intentLoader() {
        Intent intent = getIntent();
        searchInput = intent.getStringExtra(LauncherActivity.SEARCH_QUERY_SONG);
        spinnerPos = intent.getIntExtra(LauncherActivity.SPINNER_LIST, 0);
        artistText = intent.getStringExtra(LauncherActivity.ARTIST_INPUT);
        songText = intent.getStringExtra(LauncherActivity.SONG_INPUT);
        lyricsText = intent.getStringExtra(LauncherActivity.LYRICS_INPUT);
        artistSpinnerPos = intent.getIntExtra(LauncherActivity.ARTIST_SPINNER, 0);
        songSpinnerPos = intent.getIntExtra(LauncherActivity.SONG_SPINNER, 0);
        lyricsSpinnerPos = intent.getIntExtra(LauncherActivity.LYRICS_SPINNER, 0);
        ignoreMainSearch = intent.getBooleanExtra(LauncherActivity.IGNORE_MAIN_SEARCH_INPUT, false);
    }

    public void onLoadCancel(View view) {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        finish();
    }
}
