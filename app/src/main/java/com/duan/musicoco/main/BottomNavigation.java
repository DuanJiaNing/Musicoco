package com.duan.musicoco.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.OnThemeChange;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.play.PlayActivity;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.PeriodicTask;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.media.PlayView;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class BottomNavigation implements
        IBottomNavigation,
        View.OnClickListener,
        PlayServiceCallback,
        OnThemeChange {

    private final Activity activity;

    private View mContainer;
    private View mProgress;
    private ImageView mAlbum;
    private TextView mName;
    private TextView mArts;
    private PlayView mPlay;
    private ImageButton mShowList;

    private boolean isListShow = false;
    private IPlayControl controller;
    private final PeriodicTask task;

    private long mDuration;

    private final MediaManager mediaManager;
    private BitmapBuilder builder;

    BottomNavigation(Activity activity, MediaManager mediaManager) {
        this.activity = activity;
        this.mediaManager = mediaManager;
        this.builder = new BitmapBuilder(activity);

        task = new PeriodicTask(new PeriodicTask.Task() {
            @Override
            public void execute() {
                mContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        updateProgress();
                    }
                });
            }
        }, 800);
    }

    public void initView() {
        mAlbum = (ImageView) activity.findViewById(R.id.list_album);
        mName = (TextView) activity.findViewById(R.id.list_name);
        mArts = (TextView) activity.findViewById(R.id.list_arts);
        mPlay = (PlayView) activity.findViewById(R.id.list_play);
        mShowList = (ImageButton) activity.findViewById(R.id.list_list);
        mContainer = activity.findViewById(R.id.list_bottom_nav_container);
        mProgress = activity.findViewById(R.id.list_progress);

        mContainer.setOnClickListener(this);
        mShowList.setOnClickListener(this);
        mPlay.setOnClickListener(this);

    }

    @Override
    public void showPlayList() {
        isListShow = true;

    }

    @Override
    public void hidePlayList() {
        isListShow = false;

    }

    @Override
    public void songChanged(Song song, int index, boolean isNext) {
        SongInfo info = mediaManager.getSongInfo(song);
        mDuration = (int) info.getDuration();

        update();
    }

    @Override
    public void startPlay(Song song, int index, int status) {
        SongInfo info = mediaManager.getSongInfo(song);
        mDuration = (int) info.getDuration();

        task.start();

    }

    private void updateProgress() {
        int progress;
        final float phoneWidth = Utils.getMetrics(activity).widthPixels;

        try {
            progress = controller.getProgress();
            int width = (int) (phoneWidth * (progress / (float) mDuration));
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mProgress.getLayoutParams();
            params.width = width;
            mProgress.setLayoutParams(params);
            mProgress.invalidate();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateSong(SongInfo info) {
        String name = info.getTitle();
        String arts = info.getArtist();
        Bitmap b = builder.setPath(info.getAlbum_path())
                .resize(mAlbum.getHeight())
                .build()
                .getBitmap();

        if (b == null) {
            b = BitmapUtils.bitmapResizeFromResource(
                    activity.getResources(),
                    R.mipmap.default_album,
                    mAlbum.getWidth(),
                    mAlbum.getHeight());
        }

        mName.setText(name);
        mArts.setText(arts);
        mAlbum.setImageBitmap(b);

    }

    public void update() {
        if (checkNull()) {
            return;
        }

        try {

            if (controller.status() == PlayController.STATUS_PLAYING) {
                mPlay.setPlayStatus(true);
            } else {
                mPlay.setPlayStatus(false);
            }

            Song song = controller.currentSong();
            SongInfo info = mediaManager.getSongInfo(song);
            mDuration = (int) info.getDuration();
            updateProgress();
            updateSong(info);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean checkNull() {
        if (controller == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        task.stop();
    }

    public void setController(IPlayControl controller) {
        this.controller = controller;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.list_bottom_nav_container:
                activity.startActivity(new Intent(activity, PlayActivity.class));
                break;
            case R.id.list_play: {
                boolean play = mPlay.isChecked();
                try {
                    if (play) {
                        controller.resume();
                    } else controller.pause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.list_list:
                if (isListShow)
                    hidePlayList();
                else showPlayList();
                break;
        }
    }

    @Override
    public void themeChange(Theme theme) {
        switch (theme) {
            case DARK: {
                setTheme(theme);
                break;
            }
            case WHITE:
            default: {
                setTheme(theme);
                break;
            }
        }
    }

    private void setTheme(Theme theme) {
        int[] colors = new int[4];
        if (theme == Theme.DARK) {
            colors = ColorUtils.getDarkThemeColors(activity);
        } else if (theme == Theme.WHITE) {
            colors = ColorUtils.getWhiteThemeColors(activity);
        } else return;

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicTC = colors[3];

        mContainer.setBackgroundColor(mainBC);
        mName.setTextColor(mainTC);
        mArts.setTextColor(vicTC);

        mPlay.setPauseLineColor(mainTC);
        mPlay.setSolidColor(mainTC);
        mPlay.setTriangleColor(mainTC);

        mProgress.setBackgroundColor(mainTC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mShowList.getDrawable().setTint(mainTC);
        }
    }
}
