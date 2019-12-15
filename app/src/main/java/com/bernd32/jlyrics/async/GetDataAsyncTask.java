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

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GetDataAsyncTask extends AsyncTask<String, Void, Document> { // <[Input_Parameter Type], [Progress_Report Type], [Result Type]>
    private static final String TAG = "GetDataAsyncTask";
    private Exception exception;
    private int httpStatusCode;
    private AsyncTaskListener<Document> asyncTaskListener;

    public GetDataAsyncTask(AsyncTaskListener<Document> asyncTaskListener) {
        this.asyncTaskListener = asyncTaskListener;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {
        asyncTaskListener.onPreTask();
        super.onPreExecute();
    }

    @Override
    protected Document doInBackground(String... params) {
        String url = params[0];
        Log.d(TAG, "doInBackground: loading url: " + url);
        Document doc;
        String userAgent = HelperClass.getUserAgent();
        try {
            doc = Jsoup
                    .connect(url)
                    .userAgent(userAgent)
                    .referrer("https://www.google.co.jp/")
                    .get();
        } catch (Exception e) {
            if (e instanceof HttpStatusException) {
                httpStatusCode = ((HttpStatusException) e).getStatusCode();
            }
            exception = e;
            Log.d(TAG, "doInBackground: " + e.getMessage());
            return null;
        }
        return doc.outputSettings(new Document.OutputSettings().prettyPrint(false));
    }

    protected void onPostExecute(Document s) {
        super.onPostExecute(s);
        if (asyncTaskListener != null) {
            if (exception == null) {
                asyncTaskListener.onPostTask(s);
            } else {
                asyncTaskListener.onFailure(exception, httpStatusCode);
            }
        }
    }
}
