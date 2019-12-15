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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lyrics")
public class FavLyrics {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @NonNull
    @ColumnInfo(name = "title")
    private String mTitle;

    @NonNull
    @ColumnInfo(name = "description")
    private String mDescription;

    @NonNull
    @ColumnInfo(name = "url")
    private String mUrl;

    @NonNull
    @ColumnInfo(name = "img_url")
    private String mImgUrl;

    @ColumnInfo(name = "time_stamp")
    private long mTimeStamp;

    public FavLyrics(@NonNull String title, @NonNull String description,
                     @NonNull String url, @NonNull String imgUrl, long timeStamp) {
        this.mTitle = title;
        this.mDescription = description;
        this.mUrl = url;
        this.mImgUrl = imgUrl;
        this.mTimeStamp = timeStamp;
    }

    @NonNull
    public String getTitle() {
        return this.mTitle;
    }

    @NonNull
    public String getDescription() {
        return this.mDescription;
    }

    @NonNull
    public String getUrl() {
        return this.mUrl;
    }

    @NonNull
    public String getImgUrl() {
        return this.mImgUrl;
    }

    public long getTimeStamp() {return this.mTimeStamp; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}

