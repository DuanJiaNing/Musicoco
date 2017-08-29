package com.duan.musicoco.play.bottomnav;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnPlayListVisibilityChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.PlayListAdapter;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.duan.musicoco.preference.ThemeEnum.DARK;
import static com.duan.musicoco.preference.ThemeEnum.WHITE;

/**
 * Created by DuanJiaNing on 2017/7/4.
 */

public class BottomNavigationController implements
        View.OnClickListener,
        OnPlayListVisibilityChange,
        ContentUpdatable,
        ThemeChangeable {

    private static final String TAG = "BottomNavigationControl";

    private Activity activity;
    private boolean isListShowing = false;
    private boolean isListTitleHide = false;

    private CardView mViewRoot;
    private ListView mPlayList;
    private View mListTitleContainer;
    private View vDarkBg;
    private LinearLayout llRootMain;

    private PlayPreference playPreference;
    private AppPreference appPreference;

    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;
    private IPlayControl control;

    private final SongOption songOption;
    private final ListOption listOption;

    private SongOperation songOperation;
    private PlayListAdapter playListAdapter;
    private final List<SongInfo> data = new ArrayList<>();

    private boolean isAniming = false;

    public BottomNavigationController(Activity activity, DBMusicocoController dbController, MediaManager mediaManager, PlayPreference playPreference, AppPreference appPreference) {
        this.activity = activity;
        this.dbMusicoco = dbController;
        this.mediaManager = mediaManager;
        this.playPreference = playPreference;
        this.appPreference = appPreference;

        this.listOption = new ListOption(activity);
        this.songOption = new SongOption(activity, this);
    }

    public void initViews() {

        initSelfViews();
        listOption.initViews();
        songOption.initViews();

    }

    private void initSelfViews() {

        mViewRoot = (CardView) activity.findViewById(R.id.play_list);
        mPlayList = (ListView) activity.findViewById(R.id.play_play_list);

        mListTitleContainer = activity.findViewById(R.id.play_list_bar_container);
        vDarkBg = activity.findViewById(R.id.play_dark_bg);
        llRootMain = (LinearLayout) activity.findViewById(R.id.play_root_main);

        vDarkBg.setOnClickListener(this);

        initNavPosition();

    }

    private void initNavPosition() {

        ViewGroup.LayoutParams params = mViewRoot.getLayoutParams();
        final DisplayMetrics metrics = Utils.getMetrics(activity);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = metrics.heightPixels * 5 / 9;
        mViewRoot.setLayoutParams(params);

        mListTitleContainer.post(new Runnable() {
            @Override
            public void run() {
                int titleHeight = mListTitleContainer.getMeasuredHeight();
                mViewRoot.setY(metrics.heightPixels - titleHeight);

            }
        });

    }

    public void initData(IPlayControl control) {
        this.control = control;
        this.songOperation = new SongOperation(activity, control, dbMusicoco);

        initSelfDates();
        listOption.initData(control, mPlayList, dbMusicoco);
        songOption.initData(control, songOperation, mediaManager);

        update(null, null);
        songOption.updatePlayMode();
        songOption.show();
        listOption.hide();
    }

    private void initSelfDates() {

        playListAdapter = new PlayListAdapter(activity, data);
        initAdapterClickListener();
        mPlayList.setAdapter(playListAdapter);

    }

    private void initAdapterClickListener() {
        playListAdapter.setOnItemClickListener(new PlayListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position, SongInfo info) {
                try {

                    int index = control.currentSongIndex();
                    if (position == index) {
                        if (control.status() != PlayController.STATUS_PLAYING) {
                            control.resume();
                        }
                        return;
                    }

                    SongInfo in = playListAdapter.getItem(position);
                    control.play(new Song(in.getData()));

                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });

        playListAdapter.setOnRemoveClickListener(new PlayListAdapter.OnItemRemoveClickListener() {
            @Override
            public void onRemove(int position, SongInfo info) {
                Song s = new Song(info.getData());
                songOperation.removeSongFromCurrentPlayingSheet(null, s);
            }
        });
    }

    // updateColors 在 VARYING 主题时才会不断调用
    public void updateColors(int color, boolean isVarying) {

        ColorDrawable bd = new ColorDrawable(color);
        bd.setAlpha(100);
        mListTitleContainer.setBackground(bd);

        int colorA = ColorUtils.setAlphaComponent(color, 235);
        mViewRoot.setCardBackgroundColor(colorA);

        ThemeEnum t;
        if (com.duan.musicoco.util.ColorUtils.isBrightSeriesColor(colorA)) {
            t = WHITE;
        } else {
            t = DARK;
        }

        int cs[];
        switch (t) {
            case WHITE: {
                if (isVarying) {
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorWhiteThemeForPlayOptions(activity);
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2WhiteThemeTextColor(activity);
                }
                break;
            }
            case DARK:
            default: {
                if (isVarying) {
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorDarkThemeForPlayOptions(activity);
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2DarkThemeTextColor(activity);
                }
                break;
            }
        }
        songOption.setDrawableColor(cs[0]);
        listOption.setDrawableColor(cs[0]);

        if (playListAdapter != null) {
            playListAdapter.setMainTextColor(cs[0]);
            playListAdapter.setVicTextColor(cs[1]);
        }

        listOption.updateColors();
        songOption.updateColors();

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        themeEnum = appPreference.getTheme();
        int[] cs = com.duan.musicoco.util.ColorUtils.get10ThemeColors(activity, themeEnum);

        int accentC = cs[2];

        // ItemColor 随主题改变而变化
        playListAdapter.setSelectItemColor(accentC);

        songOption.themeChange(themeEnum, cs);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {

        updatePlayListAdapter();
        listOption.update(obj, completed);
        songOption.update(obj, completed);
    }

    @Override
    public void noData() {
    }

    public void noSongInService() {
        // UPDATE: 2017/8/26 更新
    }

    @Override
    public void show() {
        isListShowing = true;

        //更新列表数据（当前播放歌曲）

        int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int mb = (int) activity.getResources().getDimension(R.dimen.activity_default_margin);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        float from = mViewRoot.getY();
        float to = isListTitleHide ? from - (mViewRoot.getHeight() - mb) : from - (mViewRoot.getHeight() - mb - marginB);
        startTranslatePlayListAnim(
                from,
                to,
                duration,
                mViewRoot,
                new AccelerateDecelerateInterpolator(), new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isAniming = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        isAniming = false;

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isAniming = false;

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        songOption.hide();
        listOption.show();

        vDarkBg.setClickable(true);
        vDarkBg.setVisibility(View.VISIBLE);

        AnimationUtils.startAlphaAnim(vDarkBg, duration, null, 0.0f, 0.6f);
    }

    @Override
    public void hide() {
        isListShowing = false;

        int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        int mb = (int) activity.getResources().getDimension(R.dimen.activity_default_margin);
        float from = mViewRoot.getY();
        float to = isListTitleHide ? from + (mViewRoot.getHeight() - mb) : from + (mViewRoot.getHeight() - mb - marginB);
        startTranslatePlayListAnim(
                from,
                to,
                duration,
                mViewRoot,
                new DecelerateInterpolator(), new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        isAniming = true;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listOption.hide();
                        songOption.show();
                        isAniming = false;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        isAniming = false;

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        vDarkBg.setClickable(false);

        AnimationUtils.startAlphaAnim(vDarkBg, duration, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                vDarkBg.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }, 0.6f, 0.0f);
    }

    @Override
    public boolean visible() {
        return isListShowing;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_dark_bg:
                if (visible() && !isAniming()) {
                    hide();
                }
                break;
        }
    }

    private void updatePlayListAdapter() {
        try {
            Song song = control.currentSong();
            SongInfo info = mediaManager.getSongInfo(activity, song);
            if (info == null) {
                return;
            }

            // 更新列表数据
            List<Song> ss = control.getPlayList();
            data.clear();
            for (Song s : ss) {
                data.add(mediaManager.getSongInfo(activity, s));
            }

            // 更新当前播放
            int i = ss.indexOf(song);
            if (i != -1) {
                playListAdapter.setSelectItem(i);
            }

            // 更新是否为主歌单，非主歌单才有【移除】选项
            if (control.getPlayListId() > 0) {
                playListAdapter.setRemoveButtonEnable(true);
            } else {
                playListAdapter.setRemoveButtonEnable(false);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void updatePlayMode() {
        songOption.updatePlayMode();
    }

    public void updateFavorite() {
        try {
            Song song = control.currentSong();
            if (song != null) {
                DBSongInfo info = dbMusicoco.getSongInfo(song);
                boolean isFavorite = info != null && info.favorite;
                songOption.updateCurrentFavorite(isFavorite, false);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    boolean isListShowing() {
        return isListShowing;
    }

    public boolean isListTitleHide() {
        return isListTitleHide;
    }

    void hidePlayListTitle() {
        isListTitleHide = true;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int titleHeight = mListTitleContainer.getMeasuredHeight();
        float from = mViewRoot.getY();
        float to = from + titleHeight;

        startTranslateTitleAnim(from, to, duration);
    }

    public void showPlayListTitle() {
        isListTitleHide = false;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int titleHeight = mListTitleContainer.getMeasuredHeight();
        float from = mViewRoot.getY();
        float to = from - titleHeight;

        startTranslateTitleAnim(from, to, duration);

    }

    private void startTranslateTitleAnim(float from, float to, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);

        ValueAnimator animY = ObjectAnimator.ofFloat(from, to);

        int titleHeight = mListTitleContainer.getMeasuredHeight();
        int fromM = isListTitleHide ? titleHeight : 0;
        int toM = isListTitleHide ? 0 : titleHeight;
        ValueAnimator animM = ObjectAnimator.ofInt(fromM, toM);
        animM.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) llRootMain.getLayoutParams();
                int va = (int) animation.getAnimatedValue();
                param.setMargins(0, 0, 0, va);
                llRootMain.setLayoutParams(param);
                llRootMain.requestLayout();

            }
        });

        animY.setDuration(duration);
        animY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()

        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                mViewRoot.setY(va);
            }
        });

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAniming = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAniming = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAniming = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.play(animY).with(animM);
        set.start();
    }

    private void startTranslatePlayListAnim(float from, float to, int duration, final View view, @Nullable TimeInterpolator interpolator, @Nullable Animator.AnimatorListener listener) {
        final ValueAnimator anim = ObjectAnimator.ofFloat(from, to);
        anim.setDuration(duration);
        if (interpolator != null)
            anim.setInterpolator(interpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                view.setY(va);
            }
        });
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    public boolean isAniming() {
        return isAniming;
    }
}
