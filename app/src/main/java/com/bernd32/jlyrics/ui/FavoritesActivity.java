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

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.adapters.FavLyricsAdapter;
import com.bernd32.jlyrics.database.FavLyrics;
import com.bernd32.jlyrics.database.LyricsViewModel;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private static final String TAG = "FavoritesActivity";
    private LyricsViewModel lyricsViewModel;
    @SuppressWarnings("WeakerAccess")
    public RecyclerView recyclerView; // must not be private or static
    private FavLyricsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: has been called");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        recyclerView = findViewById(R.id.recycler_view_favs);
        TextView emptyMsg = findViewById(R.id.empty_message);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FavLyricsAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
        lyricsViewModel = new ViewModelProvider(this).get(LyricsViewModel.class);
        // Get all the lyrics from the database
        // and associate them with the adapter
        lyricsViewModel.getAllLyrics().observe(this, (List<FavLyrics> lyrics) -> {
            // Update the cached copy of the lyrics in the adapter.
            adapter.addItems(lyrics);
            // Show a message if the list is empty
            emptyMsg.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
        deleteFavoriteBySwipe();
        FavoritesActivity.this.setTitle(getString(R.string.favs));
    }

    private void deleteFavoriteBySwipe() {
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    // Delete an item from the database.
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        FavLyrics myLyrics = adapter.getItem(position);
                        Toast.makeText(FavoritesActivity.this,
                                getString(R.string.delete_lyrics_preamble), Toast.LENGTH_LONG).show();
                        lyricsViewModel.deleteLyrics(myLyrics);
                    }
                });
        // Attach the item touch helper to the recycler view
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            lyricsViewModel.deleteAll();
            adapter.clear();
            Toast.makeText(this, "Cleared", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
