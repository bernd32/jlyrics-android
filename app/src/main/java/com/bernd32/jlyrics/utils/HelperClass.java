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

package com.bernd32.jlyrics.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class HelperClass {
    static final List<String> userAgents = Arrays.asList("Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko)",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.2 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 13_1_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15G77",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");

    public static String getUserAgent() {
        return userAgents.get(ThreadLocalRandom.current().nextInt((userAgents.size())));
    }

    public static String urlBuilder(int page, int spinnerPos, String searchInput) {
        // Used for simple search
        StringBuilder url = new StringBuilder("http://search.j-lyric.net/index.php?&ct=2&ca=2&cl=2&p=");
        final int SONG_NAME_SELECTED = 0;
        final int ARTIST_SELECTED = 1;
        final int LYRICS_SELECTED = 2;
        switch (spinnerPos) {
            case SONG_NAME_SELECTED:
                url.append(page).append("&kt=").append(searchInput);
                break;
            case ARTIST_SELECTED:
                url.append(page).append("&ka=").append(searchInput);
                break;
            case LYRICS_SELECTED:
                url.append(page).append("&kl=").append(searchInput);
                break;
        }
        return url.toString();
    }

    public static String urlBuilder(int page, String artistText, int artistSpinnerPos, String songText,
                              int songSpinnerPos, String lyricsText, int lyricsSpinnerPos) {
        // Used for detailed search
        if (lyricsSpinnerPos > 0) lyricsSpinnerPos += 1;
        String url = String.format(Locale.getDefault(),
                "http://search.j-lyric.net/index.php?p=%d&kt=%s&ct=%d&ka=%s&ca=%d&kl=%s&cl=%d",
                page, songText, songSpinnerPos, artistText, artistSpinnerPos, lyricsText, lyricsSpinnerPos);
        return url;
    }

    public static String addNewLines(String html) {
        // Add new lines to the fetched text
        Document document = Jsoup.parse(html);
        // Makes html() preserve linebreaks and spacing
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String s = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }
}
