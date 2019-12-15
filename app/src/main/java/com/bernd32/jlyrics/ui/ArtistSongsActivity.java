package com.bernd32.jlyrics.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bernd32.jlyrics.Lyric;
import com.bernd32.jlyrics.PaginationListener;
import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.adapters.PostRecyclerAdapter;
import com.bernd32.jlyrics.async.GetDataAsyncTask;
import com.bernd32.jlyrics.search.CallbackInterface;
import com.bernd32.jlyrics.search.SearchLyricsViewModel;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ArtistSongsActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final String NO_IMG = "no_img";
    @SuppressWarnings("WeakerAccess")
    public RecyclerView mRecyclerView;
    private ProgressBar progressBar;
    private PostRecyclerAdapter adapter;
    private String url;
    private FloatingActionButton floatingActionButton;
    private ExtendedFloatingActionButton cancelFAB;
    private GetDataAsyncTask task;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        cancelFAB = findViewById(R.id.load_cancel);
        progressBar = findViewById(R.id.progress_bar);
        mRecyclerView = findViewById(R.id.recyclerv_view);
        floatingActionButton = findViewById(R.id.floating_action_button);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            loadIntents();
        }
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        adapter = new PostRecyclerAdapter(this, new ArrayList<>());
        mRecyclerView.setAdapter(adapter);

        SearchLyricsViewModel viewModel = new ViewModelProvider(this).get(SearchLyricsViewModel.class);
        // show progressbar only in initial loading
        CallbackInterface listener = new CallbackInterface() {

            @Override
            public void showAlertDialog() {

            }

            @Override
            public void setActivityTitle(int numberOfSongs) {
                ArtistSongsActivity.this.setTitle(getString(R.string.title_found) +
                        " " + numberOfSongs);
            }

            @Override
            public void setPageType(int pageType) {

            }

            @Override
            public void getSearchResults(ArrayList<Lyric> items) {
                adapter.addItems(items);
            }

            @Override
            public void taskStarted() {
                Log.d(TAG, "onPreTask: started");
                // show progressbar only in initial loading
                progressBar.setVisibility(View.VISIBLE);
                cancelFAB.setVisibility(View.VISIBLE);
            }

            @Override
            public void taskFinished() {
                Log.d(TAG, "onPostTask: started");
                progressBar.setVisibility(View.GONE);
                cancelFAB.setVisibility(View.GONE);
            }

            @Override
            public void taskFailed(Exception e, int statusCode) {
                Toast.makeText(ArtistSongsActivity.this,
                        getString(R.string.connection_failed),
                        Toast.LENGTH_LONG).show();
                finish();
                Log.d(TAG, "onFailure: " + e.toString());
            }
        };

        viewModel.addListener(listener);
        viewModel.newSearchRequest(url);

        floatingActionButton.setOnClickListener(view -> {
            mRecyclerView.smoothScrollToPosition(0);
        });
        mRecyclerView.addOnScrollListener(new PaginationListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                floatingActionButton.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean isLastPage() {
                return true;
            }

            @Override
            public boolean isLoading() {
                return false;
            }

            @Override
            protected void showFAB() {
                if(floatingActionButton.getVisibility() != View.VISIBLE) {
                    floatingActionButton.show();
                }
            }

            @Override
            protected void hideFAB() {
                if(floatingActionButton.getVisibility() == View.VISIBLE) {
                    floatingActionButton.hide();
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public void onLoadCancel(View view) {
        if (task != null)
            task.cancel(true);
        finish();
    }

    private void loadIntents() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        url = intent.getStringExtra("url");
        String imgUrl = intent.getStringExtra("img_url");
    }
}
