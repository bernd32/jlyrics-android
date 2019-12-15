package com.bernd32.jlyrics.search;

import com.bernd32.jlyrics.Lyric;

import java.util.ArrayList;

public interface CallbackInterface {

    void showAlertDialog();
    void setActivityTitle(int numberOfSongs);
    void setPageType(int pageType);
    void getSearchResults(ArrayList<Lyric> items);
    void taskStarted();
    void taskFinished();
    void taskFailed(Exception e, int statusCode);

}
