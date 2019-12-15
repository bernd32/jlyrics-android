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
import android.util.Log;

import com.bernd32.jlyrics.utils.HelperClass;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Used for showing furigana
 */

public class PostDataAsyncTask extends AsyncTask<String, Void, Document> { // <[Input_Parameter Type], [Progress_Report Type], [Result Type]>
    private static final String TAG = "PostDataAsyncTask";
    private Exception exception;
    private int httpStatusCode;
    private AsyncTaskListener<String> listener;

    public PostDataAsyncTask(AsyncTaskListener<String> listener) {
        this.listener = listener;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreTask();
    }

    @Override
    protected Document doInBackground(String... params) {
        // params[0] is a lyric text
        String url;
        url = "https://www.jcinfo.net/ja/tools/kana";
        Log.d(TAG, "doInBackground: params="+params[0]);
        Document doc;
        String userAgent = HelperClass.getUserAgent();
        try {

            Connection.Response response = Jsoup
                    .connect(url)
                    .method(Connection.Method.POST)
                    .userAgent(userAgent)
                    .referrer("https://www.google.co.jp/")
                    .data("txt", params[0])
                    .execute();
            doc = response.parse();
        } catch (Exception e) {
            if (e instanceof HttpStatusException) {
                httpStatusCode = ((HttpStatusException) e).getStatusCode();
            }
            exception = e;
            Log.d(TAG, "doInBackground: " + e.getMessage());
            return null;
        }
        return doc;
    }

    protected void onPostExecute(Document s) {
        String htmlString = s.select("#main-content > div.dsp2.radius_5").html();
        Log.d(TAG, "onPostExecute: \n\n" + htmlString);
        super.onPostExecute(s);
        if (listener != null) {
            if (exception == null) {
                listener.onPostTask(htmlString);
            } else {
                listener.onFailure(exception, httpStatusCode);
            }
        }

    }
}
