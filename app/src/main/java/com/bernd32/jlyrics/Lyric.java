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

package com.bernd32.jlyrics;

public class Lyric {

  private String cardTitle;
  private String cardDescription;
  private String imgUrl;
  private String songUrl;
  private String artistUrl;
  private String url;

  public String getArtistUrl() {
    return artistUrl;
  }

  public void setArtistUrl(String artistUrl) {
    this.artistUrl = artistUrl;
  }

  public String getCardTitle() {
    return cardTitle;
  }

  public void setCardTitle(String cardTitle) {
    this.cardTitle = cardTitle;
  }

  public String getCardDescription() {
    return cardDescription;
  }

  public void setCardDescription(String cardDescription) {
    this.cardDescription = cardDescription;
  }

  public String getImgUrl() {
    return imgUrl;
  }

  public void setImgUrl(String imgUrl) {
    this.imgUrl = imgUrl;
  }

  public String getSongUrl() {
    return songUrl;
  }

  public void setSongUrl(String songUrl) {
    this.songUrl = songUrl;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
