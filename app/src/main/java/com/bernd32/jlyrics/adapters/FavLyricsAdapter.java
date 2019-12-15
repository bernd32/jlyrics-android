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
import com.bernd32.jlyrics.R;
import com.bernd32.jlyrics.database.FavLyrics;
import com.bernd32.jlyrics.ui.SearchActivity;
import com.bernd32.jlyrics.ui.ShowLyricsActivity;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class FavLyricsAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    private static final String TAG = "PostRecyclerAdapter";
    private List<FavLyrics> mPostItems;
    private Context mContext;

    public FavLyricsAdapter(Context context, List<FavLyrics> postItems) {
        Log.d(TAG, "PostRecyclerAdapter: constructor activated");
        this.mPostItems = postItems;
        this.mContext = context;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBind(position);
    }


    @Override
    public int getItemCount() {
        return mPostItems == null ? 0 : mPostItems.size();
    }

    public void addItems(List<FavLyrics> postItems) {
        mPostItems.addAll(postItems);
        mPostItems = postItems;
        notifyDataSetChanged();
    }

    public void clear() {
        mPostItems.clear();
        notifyDataSetChanged();
        Glide.get(mContext).clearMemory();
    }

    public FavLyrics getItem(int position) {
        return mPostItems.get(position);
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
            FavLyrics item = mPostItems.get(position);
            cardTitle.setText(item.getTitle());
            cardDescription.setText(item.getDescription());
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
                Intent intent = new Intent(mContext, ShowLyricsActivity.class);
                intent.putExtra("song_url", item.getUrl());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("img_url", item.getImgUrl());
                mContext.startActivity(intent);
            });
        }
    }

}

