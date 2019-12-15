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
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

class LyricsRepository {

    private LyricsDao mLyricsDao;
    private LiveData<List<FavLyrics>> mAllFavLyrics;

    public LyricsRepository(Application application) {
        LyricsRoomDatabase db = LyricsRoomDatabase.getDatabase(application);
        mLyricsDao = db.lyricsDao();
        mAllFavLyrics = mLyricsDao.getFavLyricsById();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<FavLyrics>> getAllLyrics() {
        return mAllFavLyrics;
    }

    // Must call this on a non-UI thread
    public void insert(FavLyrics lyrics) {
        LyricsRoomDatabase.databaseWriteExecutor.execute(() -> {
            mLyricsDao.insert(lyrics);
        });
    }

    public void deleteAll() {
        new DeleteAllLyricsAsyncTask(mLyricsDao).execute();
    }

    // Need to run off main thread
    public void deleteLyrics(FavLyrics lyrics) {
        new DeleteLyricsAsyncTask(mLyricsDao).execute(lyrics);
    }

    public boolean isLyricsExists(String url) throws ExecutionException, InterruptedException {
        new SearchLyricsAsyncTask(mLyricsDao).execute(url);
        SearchLyricsAsyncTask searchLyricsAsyncTask = new SearchLyricsAsyncTask(mLyricsDao);
        return searchLyricsAsyncTask.execute(url).get();
    }


    public void deleteLyricsByUrl(String url) {
        new DeleteLyricsByURLAsyncTask(mLyricsDao).execute(url);
    }


    // Delete all favorites from the database (does not delete the table)
    private static class DeleteAllLyricsAsyncTask extends AsyncTask<Void, Void, Void> {
        private LyricsDao mAsyncTaskDao;

        DeleteAllLyricsAsyncTask(LyricsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

     // Delete a single favorite from the database.

    private static class DeleteLyricsAsyncTask extends AsyncTask<FavLyrics, Void, Void> {
        private LyricsDao mAsyncTaskDao;

        DeleteLyricsAsyncTask(LyricsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final FavLyrics... params) {
            mAsyncTaskDao.deleteLyrics(params[0]);
            return null;
        }
    }

    private static class SearchLyricsAsyncTask extends AsyncTask<String, Void, Boolean> {
        private LyricsDao mAsyncTaskDao;

        SearchLyricsAsyncTask(LyricsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Boolean doInBackground(final String... params) {
            int count = mAsyncTaskDao.isLyricsExists(params[0]);
            return count != 0;
        }
    }

    private static class DeleteLyricsByURLAsyncTask extends AsyncTask<String, Void, Void> {
        private LyricsDao mAsyncTaskDao;

        DeleteLyricsByURLAsyncTask(LyricsDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final String... params) {
            mAsyncTaskDao.deleteLyricsByUrl(params[0]);
            return null;
        }
    }
}
