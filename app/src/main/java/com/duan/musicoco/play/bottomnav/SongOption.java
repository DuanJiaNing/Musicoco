package com.duan.musicoco.play.bottomnav;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ToastUtils;

import java.util.Arrays;

/**
 * Created by DuanJiaNing on 2017/8/12.
 */

public class SongOption implements
        View.OnClickListener,
        ViewVisibilityChangeable,
        ThemeChangeable,
        AdapterView.OnItemClickListener,
        ContentUpdatable {

    private Activity activity;

    private ViewGroup container;
    private final OptionsDialog mDialog;
    private OptionsAdapter moreOptionsAdapter;

    private ImageButton hidePlayListBar;
    private ImageButton playMode;
    private ImageButton playFavorite;
    private ImageButton playShowList;
    private ImageButton playShowMore;

    private int currentDrawableColor;
    private IPlayControl control;
    private SongOperation songOperation;
    private BottomNavigationController controller;
    private MediaManager mediaManager;

    public SongOption(Activity activity, BottomNavigationController controller) {
        this.activity = activity;
        this.controller = controller;
        this.mDialog = new OptionsDialog(activity);

    }

    void initViews() {

        container = (ViewGroup) activity.findViewById(R.id.play_list_hide_bar);
        hidePlayListBar = (ImageButton) activity.findViewById(R.id.play_list_hide);
        playMode = (ImageButton) activity.findViewById(R.id.play_mode);
        playFavorite = (ImageButton) activity.findViewById(R.id.play_favorite);
        playShowList = (ImageButton) activity.findViewById(R.id.play_show_list);
        playShowMore = (ImageButton) activity.findViewById(R.id.play_show_more);

        hidePlayListBar.setOnClickListener(this);
        playMode.setOnClickListener(this);
        container.setOnClickListener(this);
        playFavorite.setOnClickListener(this);
        playShowList.setOnClickListener(this);
        playShowMore.setOnClickListener(this);

        mDialog.setOnItemClickListener(this);
    }

    void initData(IPlayControl control, SongOperation songOperation, MediaManager mediaManager) {
        this.songOperation = songOperation;
        this.mediaManager = mediaManager;
        this.control = control;

        moreOptionsAdapter = new OptionsAdapter(activity);
        mDialog.setAdapter(moreOptionsAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_mode:
                handlePlayModeChange();
                break;
            case R.id.play_list_hide_bar:
                if (controller.isListShowing()) {
                    controller.hide();
                } else {
                    controller.show();
                }
                break;
            case R.id.play_list_hide:
                if (!controller.isListTitleHide()) {
                    controller.hidePlayListTitle();
                }
                break;
            case R.id.play_favorite:
                handleFavoriteStatusChange();
                break;
            case R.id.play_show_list:
                if (visible()) {
                    controller.hide();
                } else {
                    controller.show();
                }
                break;
            case R.id.play_show_more:
                handleShowMore();
                break;
        }
    }

    private void handleShowMore() {
        if (mDialog.visible()) {
            mDialog.hide();
        } else {
            Song s = null;
            try {
                s = control.currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            SongInfo info = mediaManager.getSongInfo(activity, s);
            String title = activity.getString(R.string.song) + ": " + info.getTitle();
            mDialog.setTitle(title);
            mDialog.show();
        }
    }

    private void handleFavoriteStatusChange() {
        try {
            Song song = control.currentSong();
            if (song != null) {
                boolean after = songOperation.reverseSongFavoriteStatus(song);
                updateCurrentFavorite(after, true);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void handlePlayModeChange() {
        try {
            int mode = control.getPlayMode();
            mode = ((mode - 21) + 1) % 3 + 21;
            control.setPlayMode(mode);

            mode = updatePlayMode();
            StringBuilder builder = new StringBuilder();
            switch (mode) {
                case PlayController.MODE_LIST_LOOP:
                    builder.append(activity.getString(R.string.play_mode_list_loop));
                    break;

                case PlayController.MODE_SINGLE_LOOP:
                    builder.append(activity.getString(R.string.play_mode_single_loop));
                    break;

                case PlayController.MODE_RANDOM:
                    builder.append(activity.getString(R.string.play_mode_random));
                    break;
            }
            ToastUtils.showShortToast(builder.toString(), activity);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        container.setVisibility(View.VISIBLE);
        AnimationUtils.startAlphaAnim(container, 500, null, 0.0f, 1.0f);
    }

    @Override
    public void hide() {
        container.setVisibility(View.GONE);
    }

    @Override
    public boolean visible() {
        return false;
    }

    public void updateColors() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hidePlayListBar.getDrawable().setTint(currentDrawableColor);
            playMode.getDrawable().setTint(currentDrawableColor);
            playShowList.getDrawable().setTint(currentDrawableColor);
            playShowMore.getDrawable().setTint(currentDrawableColor);
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        int accentC = colors[2];
        int mainBC = colors[3];
        int vicBC = colors[4];
        int mainTC = colors[5];
        int vicTC = colors[6];

        mDialog.setTitleBarBgColor(vicBC);
        mDialog.setContentBgColor(mainBC);
        mDialog.setDivideColor(vicTC);
        mDialog.setTitleTextColor(mainTC);

        moreOptionsAdapter.setTitleColor(mainTC);
        moreOptionsAdapter.setIconColor(accentC);
    }

    int updatePlayMode() {

        Drawable drawable = null;
        int mode = PlayController.MODE_LIST_LOOP;

        try {
            mode = control.getPlayMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        switch (mode) {
            case PlayController.MODE_LIST_LOOP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = activity.getDrawable(R.drawable.list_loop);
                } else drawable = activity.getResources().getDrawable(R.drawable.list_loop);
                break;

            case PlayController.MODE_SINGLE_LOOP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = activity.getDrawable(R.drawable.single_loop);
                } else drawable = activity.getResources().getDrawable(R.drawable.single_loop);
                break;

            case PlayController.MODE_RANDOM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = activity.getDrawable(R.drawable.random);
                } else drawable = activity.getResources().getDrawable(R.drawable.random);
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.setTint(currentDrawableColor);
        }
        playMode.setImageDrawable(drawable);

        return mode;
    }


    void updateCurrentFavorite(boolean favorite, boolean useAnim) {

        Drawable select;
        Drawable notSelect;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            select = activity.getDrawable(R.drawable.ic_favorite);
            notSelect = activity.getDrawable(R.drawable.ic_favorite_border);
        } else {
            select = activity.getResources().getDrawable(R.drawable.ic_favorite);
            notSelect = activity.getResources().getDrawable(R.drawable.ic_favorite_border);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notSelect != null) {
                notSelect.setTint(currentDrawableColor);
            }

            if (select != null) {
                int color = activity.getResources().getColor(R.color.favorite);
                select.setTint(color);
            }
        }

        if (useAnim) {
            if (favorite) {
                startFavoriteSwitchAnim(select);
            } else {
                startFavoriteSwitchAnim(notSelect);
            }
        } else {
            playFavorite.setImageDrawable(favorite ? select : notSelect);
        }
    }

    private void startFavoriteSwitchAnim(final Drawable to) {
        float sFrom = 1.2f;
        float sTo = 2.0f;
        ValueAnimator scaleAnimXExp = ObjectAnimator.ofFloat(playFavorite, "scaleX", sFrom, sTo);
        ValueAnimator scaleAnimYExp = ObjectAnimator.ofFloat(playFavorite, "scaleY", sFrom, sTo);
        ValueAnimator alphaExp = ObjectAnimator.ofFloat(playFavorite, "alpha", 1.0f, 0.0f);

        ValueAnimator scaleAnimXColl = ObjectAnimator.ofFloat(playFavorite, "scaleX", sTo, sFrom);
        ValueAnimator scaleAnimYColl = ObjectAnimator.ofFloat(playFavorite, "scaleY", sTo, sFrom);
        ValueAnimator alphaColl = ObjectAnimator.ofFloat(playFavorite, "alpha", 0.0f, 1.0f);

        AnimatorSet animFrom = new AnimatorSet();
        AnimatorSet animTo = new AnimatorSet();
        AnimatorSet set = new AnimatorSet();

        animFrom.play(scaleAnimXExp).with(scaleAnimYExp).with(alphaExp);
        animTo.play(scaleAnimXColl).with(scaleAnimYColl).with(alphaColl);

        set.play(animTo).after(animFrom);
        set.setDuration(300);

        animFrom.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                playFavorite.setImageDrawable(to);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                playFavorite.setImageDrawable(to);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void updateDialogAdapter() {
        moreOptionsAdapter.clearOptions();
        moreOptionsAdapter.addOption(getIconsID(), null, getTexts(), null, null);
        mDialog.reCalcuDialogHeight();
    }

    private int[] getIconsID() {
        int[] res = {
                R.drawable.ic_create_new_folder_black_24dp,
                R.drawable.ic_art_track_black_24dp,
                R.drawable.ic_delete_forever_black_24dp,
                R.drawable.ic_clear_black_24dp
        };

        try {
            int sheetID = control.getPlayListId();
            int[] ids;
            if (sheetID < 0) {
                ids = Arrays.copyOf(res, 3);
            } else {
                ids = res;
            }
            return ids;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return res;
    }

    private String[] getTexts() {
        String[] res = {
                activity.getString(R.string.song_operation_collection_sheet),
                activity.getString(R.string.song_operation_detail),
                activity.getString(R.string.song_operation_delete),
                activity.getString(R.string.song_operation_remove_from_sheet)
        };

        try {
            int sheetID = control.getPlayListId();
            String[] sts;
            if (sheetID < 0) {
                sts = Arrays.copyOf(res, 3);
            } else {
                sts = res;
            }
            return sts;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return res;
    }

    //歌曲更多操作底部弹出对话框内项目的点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        try {
            Song song = control.currentSong();
            SongInfo info = mediaManager.getSongInfo(activity, song);
            if (info != null) {
                switch (position) {
                    case 0: // 收藏到歌单
                        songOperation.handleAddSongToSheet(info);
                        break;
                    case 1: // 查看详情
                        ActivityManager.getInstance().startSongDetailActivity(activity, song, true);
                        break;
                    case 2: //彻底删除
                        songOperation.handleDeleteSongForever(song, null);
                        break;
                    case 3: {//从歌单中移除(非主歌单才有)
                        songOperation.removeSongFromCurrentPlayingSheet(null, song);
                        break;
                    }
                    default:
                        break;
                }
            }
            mDialog.hide();

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged statusChanged) {
        updateDialogAdapter();
    }

    @Override
    public void noData() {
    }

    public void setDrawableColor(int currentDrawableColor) {
        this.currentDrawableColor = currentDrawableColor;
    }

}
