package com.duan.musicoco.fragment.album;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.play.AlbumPicture;
import com.duan.musicoco.play.PictureBuilder;
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

    private boolean isSpin = false;

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
        albumPicture = new AlbumPicture(getActivity(), albumView);
        albumView.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getActivity());
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });

    }

    @Override
    public void startSpin() {
        isSpin = true;
    }

    @Override
    public void stopSpin() {
        isSpin = false;
    }

    Bitmap def;

    @Override
    public void songChanged(Song song) {
        SongInfo info = song == null ? null : MediaManager.getInstance().getSongInfo(song, getActivity());

        if (info == null)
            return;

        int r = (Math.min(albumView.getMeasuredHeight(), albumView.getMeasuredWidth()) * 4) / 5;
        if (def == null)
            def = BitmapFactory.decodeResource(getActivity().getResources(), R.mipmap.default_album_pic);

        PictureBuilder build = new PictureBuilder(getActivity(), r, info.getAlbum_path(), def)
                .resize()
                .jpg2png()
                .toRoundBitmap()
                .build();
        albumPicture.next(build);

    }

    @Override
    public void updateSpinner() {
        Song song = new PlayPreference(getActivity()).getCurrntSong();
        songChanged(song);
        if (isSpin)
            startSpin();
    }

}
