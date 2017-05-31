package com.duan.musicoco.fragment.list;


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

public class ListFragment extends Fragment implements ViewContract {

    private PresenterContract presenter;

    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_play_list,null);
        initViews(null,null);

        return view;
    }

    @Override
    public void setPresenter(PresenterContract presenter) {
        this.presenter = presenter;
    }

    @Override
    public void initViews(@Nullable View view, Object obj) {

    }
}
