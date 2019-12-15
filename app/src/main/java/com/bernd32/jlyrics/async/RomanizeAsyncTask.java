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

package com.bernd32.jlyrics.async;


import android.os.AsyncTask;

import com.bernd32.romajihenkan.RomajiHenkan;

/**
 * Converting a Japanese text to romaji.
 * Since this operation is time-consuming, it should be performed in a non-UI thread
 */

public class RomanizeAsyncTask extends AsyncTask<String, Void, String> {

    private AsyncTaskListener<String> listener;

    public RomanizeAsyncTask(AsyncTaskListener<String> listener) {
        this.listener = listener;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreTask();
    }

    @Override
    protected void onPostExecute(String string) {
        if (listener != null) {
            listener.onPostTask(string);
        }
        super.onPostExecute(string);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected String doInBackground(String... strings) {
        RomajiHenkan henkan = new RomajiHenkan();
        return henkan.convert(strings[0]);
    }
}
