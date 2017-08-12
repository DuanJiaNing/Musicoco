package com.duan.musicoco.main.bottom;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.shared.ExceptionHandler;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnPlayListVisibilityChange;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.shared.PlayListAdapter;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.shared.PeriodicTask;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.PullDownLinearLayout;
import com.duan.musicoco.view.media.PlayView;

/**
 * Created by DuanJiaNing on 2017/6/27.
 */

public class BottomNavigationController implements
        View.OnClickListener,
        PlayServiceCallback,
        ContentUpdatable,
        OnEmptyMediaLibrary,
        ThemeChangeable {

    private final static String TAG = "BottomNavigationController";

    private final Activity activity;
    private final BroadcastManager broadcastManager;
    private IPlayControl mControl;

    private final MediaManager mediaManager;
    private final PlayPreference playPreference;

    private View mContainer;
    private View mProgress;
    private View mProgressBG;
    private ImageView mAlbum;
    private TextView mName;
    private TextView mArts;
    private PlayView mPlay;
    private ImageButton mShowList;


    private BitmapBuilder builder;
    private final PeriodicTask task;
    private ListViewsController listViewsController;

    private SongInfo currentSong;

    private boolean hasInitData = false;

    public BottomNavigationController(Activity activity, MediaManager mediaManager) {
        this.activity = activity;
        this.mediaManager = mediaManager;
        this.playPreference = new PlayPreference(activity);
        this.builder = new BitmapBuilder(activity);
        this.broadcastManager = BroadcastManager.getInstance(activity);
        this.listViewsController = new ListViewsController(activity, mediaManager);

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

        initSelfViews();
        listViewsController.initViews();

    }

    private void initSelfViews() {

        mAlbum = (ImageView) activity.findViewById(R.id.list_album);
        mName = (TextView) activity.findViewById(R.id.list_name);
        mArts = (TextView) activity.findViewById(R.id.list_arts);
        mPlay = (PlayView) activity.findViewById(R.id.list_play);
        mShowList = (ImageButton) activity.findViewById(R.id.list_list);
        mContainer = activity.findViewById(R.id.list_bottom_nav_container);
        mProgress = activity.findViewById(R.id.list_progress);
        mProgressBG = activity.findViewById(R.id.list_progress_bg);

        mContainer.setOnClickListener(this);
        mShowList.setOnClickListener(this);
        mPlay.setOnClickListener(this);

    }

    @Override
    public void dataIsReady(IPlayControl control) {
        Log.d(TAG, "dataIsReady: ");
    }

    public void initData(IPlayControl control, DBMusicocoController dbController) {
        this.mControl = control;

        initSelfData();
        listViewsController.initData(control, dbController);

        hasInitData = true;

    }

    private void initSelfData() {

        mContainer.setEnabled(true);
        mPlay.setEnabled(true);
        mShowList.setEnabled(true);

    }

    public boolean hasInitData() {
        return hasInitData;
    }

    @Override
    public void songChanged(Song song, int index, boolean isNext) {
        if (song == null) {
            //播放列表是空的
            return;
        }

        currentSong = mediaManager.getSongInfo(song);
        update(null, null);

        // 如果此时正在浏览歌单详情 SheetDetailActivity ，需要通知更新
        BroadcastManager.getInstance(activity).sendMyBroadcast(BroadcastManager.FILTER_SHEET_DETAIL_SONGS_CHANGE, null);
    }

    private void updateProgress() {
        int progress;
        int duration = (int) currentSong.getDuration();
        final float phoneWidth = Utils.getMetrics(activity).widthPixels;

        try {
            progress = mControl.getProgress();
            int width = (int) (phoneWidth * (progress / (float) duration));
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mProgress.getLayoutParams();
            params.width = width;
            mProgress.setLayoutParams(params);
            mProgress.invalidate();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateSongInfo() {

        Song song = new Song(currentSong.getData());
        SongInfo info = mediaManager.getSongInfo(song);

        String name = info.getTitle();
        String arts = info.getArtist();
        builder.reset();
        Bitmap b = builder.setPath(info.getAlbum_path())
                .resize(mAlbum.getHeight())
                .build()
                .getBitmap();

        if (b == null) {
            b = BitmapUtils.getDefaultPictureForAlbum(
                    mAlbum.getWidth(),
                    mAlbum.getHeight());
        }

        mName.setText(name);
        mArts.setText(arts);
        mAlbum.setImageBitmap(b);

    }

    @Override
    public void update(@Nullable Object obj, OnUpdateStatusChanged completed) {

        try {

            if (mControl.status() == PlayController.STATUS_PLAYING) {
                mPlay.setPlayStatus(true);
            } else {
                mPlay.setPlayStatus(false);
            }

            updateProgress();
            updateSongInfo();
            listViewsController.update(currentSong, null);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startPlay(Song song, int index, int status) {
        currentSong = mediaManager.getSongInfo(song);

        task.start();
        mPlay.setChecked(true);

        //??
        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        task.stop();
        mPlay.setChecked(false);

        //??
        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
    }

    @Override
    public void onPlayListChange(Song current, int index, int id) {
        update(null, null);
        playPreference.updateSheet(id);

        //发送广播通知 MySheetController 更新列表（列表的选中播放状态）
        broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.list_bottom_nav_container:
                ActivityManager.getInstance(activity).startPlayActivity();
                break;
            case R.id.list_play: {
                boolean play = mPlay.isChecked();
                try {
                    if (play) {
                        mControl.resume();
                    } else mControl.pause();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.list_list:
                if (listViewsController.visible()) {
                    listViewsController.hide();
                } else {
                    listViewsController.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int[] cs = ColorUtils.get10ThemeColors(activity, themeEnum);

        int statusC = cs[0];
        int toolbarC = cs[1];
        int accentC = cs[2];
        int mainBC = cs[3];
        int vicBC = cs[4];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int navC = cs[7];
        int toolbarMainTC = cs[8];
        int toolbarVicTC = cs[9];

        listViewsController.themeChange(themeEnum, cs);

        mContainer.setBackgroundColor(navC);
        mName.setTextColor(mainTC);
        mArts.setTextColor(vicTC);

        mPlay.setPauseLineColor(mainTC);
        mPlay.setSolidColor(mainTC);
        mPlay.setTriangleColor(mainTC);

        mProgress.setBackgroundColor(accentC);

        ColorDrawable cd = new ColorDrawable(vicBC);
        cd.setAlpha(200);
        mProgressBG.setBackground(cd);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mShowList.getDrawable().setTint(mainTC);
        }
    }

    @Override
    public void emptyMediaLibrary() {
        mContainer.setEnabled(false);
        mPlay.setEnabled(false);
        mShowList.setEnabled(false);
    }

}
