package com.duan.musicoco.fragment.album;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.image.AlbumPicture;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.preference.PlayPreference;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerFragment extends Fragment implements ViewContract {

    public static final String TAG = "VisualizerFragment";

    private PresenterContract presenter;

    private View view;

    private ImageSwitcher albumView;

    private AlbumPicture albumPicture;

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

        albumView = (ImageSwitcher) view.findViewById(R.id.play_album_is);
        albumView.setInAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
        albumView.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
        albumView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getActivity());
                imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int pad = (int) getActivity().getResources().getDimension(R.dimen.play_album_padding);
                imageView.setPadding(pad, pad, pad, pad);
                return imageView;
            }
        });
        //防止 getWidth == 0
        albumView.post(new Runnable() {
            @Override
            public void run() {
                albumPicture = new AlbumPicture(getActivity(), albumView);
                Song song = new PlayPreference(getActivity()).getCurrntSong();
                songChanged(song);
            }
        });

    }

    @Override
    public void startSpin() {
        albumPicture.startSpin();
    }

    @Override
    public void stopSpin() {
        albumPicture.stopSpin();
    }

    @Override
    public void songChanged(Song song) {
        SongInfo info = song == null ? null : MediaManager.getInstance().getSongInfo(song, getActivity());
        if (info == null)
            return;
        albumPicture.pre(info);

    }

    @Override
    public void updateSpinner() {
        if (albumPicture != null && albumPicture.isSpin())
            startSpin();
    }

}
