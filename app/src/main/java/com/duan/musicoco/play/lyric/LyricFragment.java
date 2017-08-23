package com.duan.musicoco.play.lyric;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class LyricFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "LyricFragment";


    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play_lyric, null);
        initViews(view, null);
        return view;
    }

    public void initViews(@Nullable View view, Object obj) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
}
