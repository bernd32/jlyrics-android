package com.bernd32.jlyrics.search;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import com.bernd32.jlyrics.Lyric;
import com.bernd32.jlyrics.adapters.PostRecyclerAdapter;
import com.bernd32.jlyrics.async.AsyncTaskListener;
import com.bernd32.jlyrics.async.GetDataAsyncTask;


import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import static com.bernd32.jlyrics.adapters.PostRecyclerAdapter.ARTISTS_PAGE;
import static com.bernd32.jlyrics.adapters.PostRecyclerAdapter.SONG_PAGE;
import static com.bernd32.jlyrics.ui.SearchActivity.NO_IMG;

class SearchLyricsRepository {

    private static final String TAG = "SearchLyricsRepository";
    private CallbackInterface listener;
    private int pageType;
    private int maxPage;

    SearchLyricsRepository(Application application) {
    }

    void newSearchRequest(String url) {
        getDocumentAsyncTask(url);
    }

    private ArrayList<Lyric> artistSearch(Document doc) {
        // Handles artist search result
        final ArrayList<Lyric> items = new ArrayList<>();
        Elements imgElements = doc.select("#mnb > div.bdy");
        Elements artists = doc.select("#mnb > div > p.mid > a");
        Elements urls = doc.select("#mnb > div > p.mid > a");
        Elements lyricsNumber = doc.select("#mnb > div > p.sml:contains(収録数：)");
        int maxPages = 1;
        // Show alert dialog if nothing found
        if (artists.size() == 0) {
            listener.showAlertDialog();
        } else {
            for (int i = 0; i < artists.size(); i++) {
                Lyric postItem = new Lyric();
                String getArtist = artists.get(i).text();
                String getNumberOfSongs = lyricsNumber.get(i).text()
                        .replace("収録数：", "Lyrics: ")
                        .replace("曲", "");
                String imgSrc = imgElements.get(i).select("a > img").attr("src");
                if (!imgSrc.isEmpty()) {
                    postItem.setImgUrl(imgSrc);
                } else {
                    postItem.setImgUrl(NO_IMG);
                }
                String artistUrl = urls.get(i).attr("href");
                postItem.setCardTitle(getArtist);
                postItem.setCardDescription(getNumberOfSongs);
                postItem.setArtistUrl(artistUrl);
                items.add(postItem);
            }
            listener.setActivityTitle(artists.size());
            setMaxPage(maxPage);
        }
        listener.getSearchResults(items);
        return items;
    }

    private ArrayList<Lyric> songSearch(Document doc) {
        Log.d(TAG, "songSearch: started");
        final ArrayList<Lyric> items = new ArrayList<>();
        maxPage = 1;
        Elements imgElements = doc.select("#mnb > div.bdy");
        Elements urls = doc.select("#mnb > div > p.mid > a");
        Elements songNames = doc.select("#mnb > div.bdy > p.mid");
        Elements artists = doc.select("#mnb > div.bdy > p.sml:contains(歌：)");
        Element pageUrlElement = doc.select("#pager > a").first();
        // Show alert dialog if nothing found
        if (artists.size() == 0) {
            listener.showAlertDialog();
        } else {
            for (int i = 0; i < artists.size(); i++) {
                Lyric lyric = new Lyric();
                String imgSrc = imgElements.get(i).select("a > img").attr("src");
                if (!imgSrc.isEmpty()) {
                    lyric.setImgUrl(imgSrc);
                } else {
                    lyric.setImgUrl(NO_IMG);
                }
                String url = urls.get(i).attr("href");
                lyric.setSongUrl(url);
                lyric.setCardTitle(songNames.get(i).text());
                lyric.setCardDescription(artists.get(i).text().replace("歌：", ""));
                items.add(lyric);
                maxPage = numberOfPages(getNumberOfSongsFound(pageUrlElement));
                setMaxPage(maxPage);
            }
            listener.setActivityTitle(getNumberOfSongsFound(pageUrlElement));
        }
        listener.getSearchResults(items);
        return items;
    }

    private int getPageType(Document doc) {
        String description = doc.select("meta[name=description]").first().attr("content");
        Elements lyricsNumber = doc.select("#mnb > div > p.sml:contains(収録数：)");
        // keywords for detecting a page type
        final String search_result_keyword = "検索結果ページ";
        final String lyrics_page_keyword = "歌詞ページ";
        final String artist_list_keyword = "歌詞一覧ページ";
        final String artist_search_result_keyword = "歌手名に";
        if (description.contains(search_result_keyword) && lyricsNumber.isEmpty()) {
            // Song search result (has pages)
            Log.d(TAG, "getPageType: Search result page detected");
            pageType = PostRecyclerAdapter.SONG_PAGE;
            //adapter.setPageType(pageType);
            listener.setPageType(pageType);
            return pageType;

        } else if (description.contains(lyrics_page_keyword)) {
            Log.d(TAG, "getPageType: Lyrics page detected");
            pageType = PostRecyclerAdapter.LYRICS_PAGE;
            //adapter.setPageType(pageType);
            return pageType;
        } else if (description.contains(artist_list_keyword)) {
            Log.d(TAG, "getPageType: Artist list page detected");
            pageType = PostRecyclerAdapter.ARTIST_LYRICS_PAGE;
            listener.setPageType(pageType);
            return pageType;
        } else if(description.contains(artist_search_result_keyword) && !lyricsNumber.isEmpty()) {
            Log.d(TAG, "getPageType: Artist search result page detected");
            pageType = PostRecyclerAdapter.ARTISTS_PAGE;
            listener.setPageType(pageType);
            return pageType;
        } else {
            Log.d(TAG, "getPageType: cannot detect page type");
            return PostRecyclerAdapter.SONG_PAGE;
        }
    }

    private void getDocumentAsyncTask(String url) {
        Log.d(TAG, "startAsyncTask: " + url);
        GetDataAsyncTask task = new GetDataAsyncTask(new AsyncTaskListener<Document>() {
            @Override
            public void onPreTask() {
                listener.taskStarted();
            }

            @Override
            public void onPostTask(Document doc) {
                pageType = getPageType(doc);
                // Get lyrics from a song search type
                if (pageType == SONG_PAGE) {
                    songSearch(doc);
                } else if (pageType == ARTISTS_PAGE) {
                    artistSearch(doc);
                } else {
                    artistSongList(doc);
                }
                listener.taskFinished();
            }

            @Override
            public void onFailure(Exception e, int statusCode) {
                listener.taskFailed(e, statusCode);
            }
        });
        task.execute(url);
    }

    private ArrayList<Lyric> artistSongList(Document doc) {
        // Handles artist's list of lyrics
        final ArrayList<Lyric> items = new ArrayList<>();
        Elements imgElements = doc.select("#mnb > div.cnt > div[id^=ly]");
        Elements titles = doc.select("#mnb > div.cnt > div[id^=ly] > p.ttl > a");
        String description = doc.selectFirst("#mnb > div.cnt > div.cap > h2").text();
        for (int i = 0; i < titles.size(); i++) {
            Lyric postItem = new Lyric();
            String imgSrc = imgElements.get(i).select("a > img.i5r").attr("src");
            if (!imgSrc.isEmpty()) {
                Log.d(TAG, i + " = " + imgSrc);
                postItem.setImgUrl(imgSrc);
            } else {
                Log.d(TAG, "artistDocumentHandle: no image found");
                postItem.setImgUrl(NO_IMG);
            }
            String title = titles.get(i).text();
            String url = titles.get(i).attr("href");
            postItem.setCardTitle(title);
            postItem.setCardDescription(description.replace("の歌詞リスト",  "")); // leave blank for now
            postItem.setSongUrl("http://j-lyric.net" + url);
            Log.d(TAG, "documentHandler: url = http://j-lyric.net" + url);
            items.add(postItem);
        }
        listener.setActivityTitle(titles.size());
        listener.getSearchResults(items);
        return items;
    }

    private int getNumberOfSongsFound(Element pageUrlElement) {
        String url = pageUrlElement.attr("href");
        Uri uri = Uri.parse(url);
        String c = uri.getQueryParameter("c");
        assert c != null;
        return Integer.valueOf(c);
    }

    private int numberOfPages(int songs) {
        double p = Math.ceil((double) songs/20);
        return (int) p;
    }

    public void addListener(CallbackInterface listener) {
        this.listener = listener;

    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    public int getMaxPage() {
        return maxPage;
    }
}
