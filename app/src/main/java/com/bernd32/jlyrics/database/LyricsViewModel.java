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

package com.bernd32.jlyrics.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * View Model to keep a reference to the lyrics repository and
 * an up-to-date list of all words.
 */

public class LyricsViewModel extends AndroidViewModel {

    private LyricsRepository mRepository;
    // Using LiveData and caching what getFavLyricsById returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<FavLyrics>> mAllLyrics;

    public LyricsViewModel(Application application) {
        super(application);
        mRepository = new LyricsRepository(application);
        mAllLyrics = mRepository.getAllLyrics();
    }

    public LiveData<List<FavLyrics>> getAllLyrics() {
        return mAllLyrics;
    }

    public void insert(FavLyrics lyrics) {
        mRepository.insert(lyrics);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public void deleteLyrics(FavLyrics lyrics) {
        mRepository.deleteLyrics(lyrics);
    }

    public boolean isLyricsExists(String url) throws ExecutionException, InterruptedException {
        return mRepository.isLyricsExists(url);
    }

    public void deleteLyricsByUrl(String url)  {
        mRepository.deleteLyricsByUrl(url);
    }
}
