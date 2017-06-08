package com.duan.musicoco.fragment.album;


import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.media.MediaManager;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.play.PlayActivity;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.view.AlbumVisualizerSurfaceView;

/**
 * Created by DuanJiaNing on 2017/5/30.
 */

public class VisualizerFragment extends Fragment implements ViewContract {

    public static final String TAG = "VisualizerFragment";

    private PresenterContract presenter;

    private View view;

    private AlbumVisualizerSurfaceView albumView;

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
        LinearLayout con = (LinearLayout) view.findViewById(R.id.play_album_visualizer_contain);
        albumView = new AlbumVisualizerSurfaceView(getActivity());
        con.addView(albumView);
    }

    @Override
    public void startSpin() {
        albumView.startSpin();
        isSpin = true;
    }

    @Override
    public void stopSpin() {
        albumView.stopSpin();
        isSpin = false;
    }

    @Override
    public void songChanged(Song song) {
        albumView.setSong(MediaManager.getInstance().getSongInfo(song, getActivity()));
    }

    @Override
    public void updateSpinner() {
        Song song = new PlayPreference(getActivity()).getCurrntSong();
        SongInfo info = song == null ? null : MediaManager.getInstance().getSongInfo(song, getActivity());
        albumView.createSurface(info);
        if (isSpin)
            albumView.startSpin();
    }

    @Override
    public AlbumVisualizer.OnUpdateVisualizerListener getVisualizerListener() {
        return albumView;
    }
}
