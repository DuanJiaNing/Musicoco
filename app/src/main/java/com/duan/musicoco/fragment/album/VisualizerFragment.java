package com.duan.musicoco.fragment.album;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.play.AlbumPicture;
import com.duan.musicoco.preference.PlayPreference;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerFragment extends Fragment implements ViewContract {

    public static final String TAG = "VisualizerFragment";

    private PresenterContract presenter;

    private View view;

    private ImageView albumView;

    private boolean isSpin = false;

    private RotateAnimation rotateAnimation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //不要让 presenter 控制初始化 View ，此时 view 可能还没执行完 inflate
        view = inflater.inflate(R.layout.fragment_play_visualizer, null);
        initViews(view, null);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSpinner();
    }

    @Override
    public void setPresenter(PresenterContract presenter) {
        this.presenter = presenter;
    }

    @Override
    public void initViews(@Nullable View view, Object obj) {

        albumView = (ImageView) view.findViewById(R.id.play_album_image);
        albumView.post(new Runnable() {
            @Override
            public void run() {
                rotateAnimation = new RotateAnimation(0, 360, albumView.getMeasuredWidth() / 2, albumView.getMeasuredHeight() / 2);
                rotateAnimation.setFillAfter(true);
                rotateAnimation.setDuration(1000 * 40);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                rotateAnimation.setRepeatCount(Animation.INFINITE);
                rotateAnimation.setRepeatMode(Animation.RESTART);
            }
        });

    }

    @Override
    public void startSpin() {
        albumView.startAnimation(rotateAnimation);
        isSpin = true;
    }

    @Override
    public void stopSpin() {
        rotateAnimation.cancel();
        isSpin = false;
    }

    @Override
    public void songChanged(Song song) {
        SongInfo info = song == null ? null : MediaManager.getInstance().getSongInfo(song, getActivity());

        if (info == null)
            return;

        int r = (Math.min(albumView.getMeasuredHeight(), albumView.getMeasuredWidth()) * 4) / 5;
        new AlbumPicture.Builder(getActivity(), r, info.getAlbum_path())
                .resize()
                .jpg2png()
                .toRoundBitmap()
                .build(albumView);

    }

    @Override
    public void updateSpinner() {
        Song song = new PlayPreference(getActivity()).getCurrntSong();
        songChanged(song);
        if (isSpin)
            startSpin();
    }

}
