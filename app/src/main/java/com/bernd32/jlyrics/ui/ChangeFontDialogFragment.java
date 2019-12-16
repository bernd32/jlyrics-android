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

package com.bernd32.jlyrics.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.bernd32.jlyrics.R;

import java.util.Objects;

public class ChangeFontDialogFragment extends DialogFragment {
    private static final String TAG = "ChangeFontDialogFragmen";
    private OnDialogButtonClick listener;

    public void setOnDialogButtonClick(OnDialogButtonClick buttonClick) {
        this.listener = buttonClick;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Instantiate the NoticeDialogListener so we can send events to the host
        listener = (OnDialogButtonClick) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: started");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: started");
        return inflater.inflate(R.layout.dialog_change_fontsize, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: started");
        super.onViewCreated(view, savedInstanceState);


    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: started");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams") View content =  inflater.inflate(R.layout.dialog_change_fontsize, null);
        builder.setView(content);
        final TextView fontSizeTV = content.findViewById(R.id.font_size_textview);
        final SeekBar seekBar = content.findViewById(R.id.seek_bar);
        TextView lyricsTV = ((ShowLyricsActivity) getActivity()).lyricsTV;
        // Convert received font size from px to sp metrics
        float px = lyricsTV.getTextSize();
        float sp = px / getResources().getDisplayMetrics().scaledDensity;
        seekBar.setProgress((int) sp);
        fontSizeTV.setText(String.valueOf((int) sp));
        builder.setMessage(R.string.change_font)
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    listener.onOkClicked(ChangeFontDialogFragment.this);
                    // Send data (seekBar.getProgress()) through the interface
                    // (OnDialogButtonClick.changeFontSize()) back to the activity (ShowLyricsActivty)
                    listener.changeFontSize(seekBar.getProgress());
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // Send the negative button event back to the host activity
                    listener.onCancelClicked(ChangeFontDialogFragment.this);
                });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fontSizeTV.setText(" " + progress);
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public interface OnDialogButtonClick {
        void onOkClicked(DialogFragment dialog);
        void onCancelClicked(DialogFragment dialog);
        void changeFontSize(int i);
    }
}
