package com.duan.musicoco.play;

import com.duan.musicoco.aidl.Song;

/**
 * Created by DuanJiaNing on 2017/5/25.
 */

public class PresenterImpl implements Contract.Presenter {

    private Contract.View mView;

    public PresenterImpl(Contract.View view) {
        this.mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void songChanged(Song song, int index) {
        mView.songChanged(song,index);
    }
}
