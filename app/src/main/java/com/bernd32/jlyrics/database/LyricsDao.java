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

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LyricsDao {

    // Notifies its active observers when the data has changed.

    @Query("SELECT * from lyrics ORDER BY id DESC")
    LiveData<List<FavLyrics>> getFavLyricsById();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(FavLyrics lyrics);

    @Query("DELETE FROM lyrics")
    void deleteAll();

    @Delete
    void deleteLyrics(FavLyrics lyrics);

    @Query("SELECT count(*) FROM lyrics WHERE url = :url")
    int isLyricsExists(String url);

    @Query("DELETE FROM lyrics WHERE url = :url")
    void deleteLyricsByUrl(String url);
}
