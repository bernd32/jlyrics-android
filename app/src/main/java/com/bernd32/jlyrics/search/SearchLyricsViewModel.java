package com.bernd32.jlyrics.search;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;

public class SearchLyricsViewModel extends AndroidViewModel {

    private SearchLyricsRepository mRepository;
    private static final String TAG = "SearchLyricsViewModel";

    public SearchLyricsViewModel(Application application) {
        super(application);
        mRepository = new SearchLyricsRepository(application);
    }

    public void newSearchRequest(String url) {
        Log.d(TAG, "newSearchRequest: " + url);
        mRepository.newSearchRequest(url);
    }

    public void addListener(CallbackInterface callback) {
        mRepository.addListener(callback);
    }

    public int getMaxPage() {
        Log.d(TAG, "getMaxPage: " + mRepository.getMaxPage());
        return mRepository.getMaxPage();
    }
}
