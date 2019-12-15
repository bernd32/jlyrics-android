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

package com.bernd32.jlyrics.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bernd32.jlyrics.BaseViewHolder;
import com.bernd32.jlyrics.Lyric;
import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.ui.ArtistSongsActivity;
import com.bernd32.jlyrics.ui.SearchActivity;
import com.bernd32.jlyrics.ui.ShowLyricsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PostRecyclerAdapter";
    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    public static final int SONG_PAGE = 0;
    public static final int ARTIST_LYRICS_PAGE = 1;
    public static final int ARTISTS_PAGE = 2;
    public static final int LYRICS_PAGE = 3;
    private boolean isLoaderVisible = false;
    private List<Lyric> mPostItems;
    private Context mContext;
    private int pageType;

    public PostRecyclerAdapter(Context context, List<Lyric> postItems) {
        Log.d(TAG, "PostRecyclerAdapter: constructor activated");
        this.mPostItems = postItems;
        this.mContext = context;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_NORMAL:
                return new ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false));
            case VIEW_TYPE_LOADING:
                return new ProgressHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoaderVisible) {
            return position == mPostItems.size() - 1 ? VIEW_TYPE_LOADING : VIEW_TYPE_NORMAL;
        } else {
            return VIEW_TYPE_NORMAL;
        }
    }

    @Override
    public int getItemCount() {
        return mPostItems == null ? 0 : mPostItems.size();
    }

    public void addItems(List<Lyric> postItems) {
        mPostItems.addAll(postItems);
        notifyDataSetChanged();
    }

    public void addLoading() {
        isLoaderVisible = true;
        mPostItems.add(new Lyric());
        notifyItemInserted(mPostItems.size() - 1);
    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = mPostItems.size() - 1;
        Lyric item = getItem(position);
        if (item != null) {
            mPostItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        Log.d(TAG, "clear: started");
        // use it when performing new search
        mPostItems.clear();
        notifyDataSetChanged();
        Glide.get(mContext).clearMemory();
    }

    private Lyric getItem(int position) {
        return mPostItems.get(position);
    }

    public void setPageType(int pageType) {
        this.pageType = pageType;
    }

    public class ViewHolder extends BaseViewHolder {
        TextView cardTitle;
        TextView cardDescription;
        ImageView image;
        MaterialCardView parentLayout;

        ViewHolder(View itemView) {
            super(itemView);
            cardTitle = itemView.findViewById(R.id.card_title);
            cardDescription = itemView.findViewById(R.id.card_description);
            image = itemView.findViewById(R.id.image);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }

        protected void clear() {

        }

        public void onBind(int position) {
            super.onBind(position);
            Lyric item = mPostItems.get(position);
            cardTitle.setText(item.getCardTitle());
            cardDescription.setText(item.getCardDescription());
            Log.d(TAG, "onBind: item#"+position+" img url="+ item.getImgUrl());
            // If no picture found, set default picture
            if (item.getImgUrl().equals(SearchActivity.NO_IMG)) {
                image.setVisibility(View.GONE);
            } else {
                image.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .asBitmap()
                        .load(item.getImgUrl())
                        .into(image);
            }
            // click listener
            parentLayout.setOnClickListener(view -> {
                Log.d(TAG, "onBind: " + pageType);
                if (pageType == SONG_PAGE || pageType == ARTIST_LYRICS_PAGE) {
                    Intent intent = new Intent(mContext, ShowLyricsActivity.class);
                    intent.putExtra("song_url", item.getSongUrl());
                    intent.putExtra("title", item.getCardTitle());
                    intent.putExtra("description", item.getCardDescription());
                    intent.putExtra("img_url", item.getImgUrl());
                    mContext.startActivity(intent);
                } else {
                    Intent intent = new Intent(mContext, ArtistSongsActivity.class);
                    intent.putExtra("url", item.getArtistUrl());
                    intent.putExtra("title", item.getCardTitle());
                    intent.putExtra("description", item.getCardDescription());
                    intent.putExtra("img_url", item.getImgUrl());
                    mContext.startActivity(intent);
                }

            });
        }
    }

    protected class ProgressHolder extends BaseViewHolder {
        ProgressHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void clear() {
        }
    }
}
