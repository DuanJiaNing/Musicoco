package com.duan.musicoco.play.bottom;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.ExceptionHandler;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnPlayListVisibilityChange;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.ViewVisibilityChangeable;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.PlayListAdapter;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealTimeBlurView;

import java.util.ArrayList;
import java.util.Arrays;
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
        OnEmptyMediaLibrary,
        ThemeChangeable {

    private static final String TAG = "BottomNavigationControl";

    private Activity activity;
    private boolean isListShowing = false;
    private boolean isListTitleHide = false;

    private CardView mViewRoot;
    private LinearLayout mPlayListContainer;
    private ListView mPlayList;
    private RealTimeBlurView mRealTimeView;
    private View mListTitleContainer;
    private View vDarkBg;
    private LinearLayout llRootMain;

    private PlayPreference playPreference;
    private AppPreference appPreference;

    private DBMusicocoController dbMusicoco;
    private IPlayControl control;
    private MediaManager mediaManager;

    private final SongOption songOption;
    private final ListOption listOption;

    private SongOperation songOperation;
    private PlayListAdapter playListAdapter;
    private final List<SongInfo> data = new ArrayList<>();

    public BottomNavigationController(Activity activity) {
        this.activity = activity;
        this.listOption = new ListOption(activity);
        this.songOption = new SongOption(activity, this);
    }

    public void initViews() {
        listOption.initViews();
        songOption.initViews();

        mViewRoot = (CardView) activity.findViewById(R.id.play_list);
        mPlayList = (ListView) activity.findViewById(R.id.play_play_list);

        mRealTimeView = (RealTimeBlurView) activity.findViewById(R.id.play_blur);
        mListTitleContainer = activity.findViewById(R.id.play_list_bar_container);
        vDarkBg = activity.findViewById(R.id.play_dark_bg);
        llRootMain = (LinearLayout) activity.findViewById(R.id.play_root_main);

        vDarkBg.setOnClickListener(this);

        ViewGroup.LayoutParams params = mViewRoot.getLayoutParams();
        DisplayMetrics metrics = Utils.getMetrics(activity);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = metrics.heightPixels * 5 / 9;
        mViewRoot.setX(0);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        mViewRoot.setY(metrics.heightPixels - marginB);
        mViewRoot.setLayoutParams(params);

        mPlayListContainer = (LinearLayout) activity.findViewById(R.id.play_nav_container);

    }

    public void initData(IPlayControl control, DBMusicocoController dbMusicoco,
                         MediaManager mediaManager, PlayPreference playPreference,
                         AppPreference appPreference) {
        this.control = control;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.playPreference = playPreference;
        this.appPreference = appPreference;
        this.songOperation = new SongOperation(activity, control, dbMusicoco);

        listOption.initData(control, mPlayList, dbMusicoco);
        songOption.initData(songOperation);

        playListAdapter = new PlayListAdapter(activity, data);
        initAdapterClickListener();
        mPlayList.setAdapter(playListAdapter);

        //更新播放列表字体颜色模式（亮 暗）
        ThemeEnum themeEnum = playPreference.getTheme();
        int alpha = activity.getResources().getInteger(R.integer.play_list_bg_alpha);
        int color;
        switch (themeEnum) {
            case DARK: {
                color = activity.getResources().getColor(R.color.theme_dark_vic_text);
                break;
            }
            case VARYING:
            case WHITE:
            default:
                color = activity.getResources().getColor(R.color.theme_white_main_text);
                break;
        }
        color = ColorUtils.setAlphaComponent(color, alpha);
        mRealTimeView.setOverlayColor(color);
        mListTitleContainer.setBackgroundColor(ColorUtils.setAlphaComponent(mRealTimeView.getOverlayColor(), 255));

        songOption.updatePlayMode();
        songOption.show();
        listOption.hide();

        int count = mViewRoot.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mViewRoot.getChildAt(i);
            v.setEnabled(true);
        }

        update(null, null);
    }

    private void initAdapterClickListener() {
        mPlayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {

                    int index = control.currentSongIndex();
                    if (position == index) {
                        Log.d(TAG, "onClick: the song is playing");
                        if (control.status() != PlayController.STATUS_PLAYING)
                            control.resume();
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

                // PlayListAdapter 在 MainActivity 和 PlayActivity 中都有，所以需要发送广播通知 MainActivity 更新
                // 【我的歌单】 列表
                BroadcastManager.getInstance(activity).sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_CHANGED, null);

            }
        });
    }

    @Override
    public void show() {
        isListShowing = true;

        //更新列表数据（当前播放歌曲）
        playListAdapter.notifyDataSetChanged();

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
                new AccelerateInterpolator(), null);

        songOption.hide();
        listOption.show();

        vDarkBg.setClickable(true);
        vDarkBg.setVisibility(View.VISIBLE);

        if (playPreference.getTheme() == WHITE) {
            AnimationUtils.startAlphaAnim(vDarkBg, duration, null, 0.0f, 0.6f);
        } else {
            vDarkBg.setBackgroundColor(Color.TRANSPARENT);
        }

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

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listOption.hide();
                        songOption.show();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

        vDarkBg.setClickable(false);

        if (playPreference.getTheme() == WHITE) {
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
        } else {
            vDarkBg.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean visible() {
        return isListShowing;
    }

    public void hidePlayListTitle() {
        isListTitleHide = true;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        float from = mViewRoot.getY();
        float to = from + marginB;

        startTranslateTitleAnim(from, to, duration);
    }

    public void showPlayListTitle() {
        isListTitleHide = false;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        float from = mViewRoot.getY();
        float to = from - marginB;

        startTranslateTitleAnim(from, to, duration);

    }

    private void startTranslateTitleAnim(float from, float to, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);

        ValueAnimator animY = ObjectAnimator.ofFloat(from, to);

        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        int fromM = isListTitleHide ? marginB : 0;
        int toM = isListTitleHide ? 0 : marginB;
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

        set.play(animY).

                with(animM);
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
        if (listener != null)
            anim.addListener(listener);
        anim.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_dark_bg:
                if (visible()) {
                    hide();
                }
                break;
        }
    }

    public boolean isListTitleHide() {
        return isListTitleHide;
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        themeEnum = appPreference.getTheme();
        int[] cs = com.duan.musicoco.util.ColorUtils.get10ThemeColors(activity, themeEnum);

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

        playListAdapter.setMainTextColor(mainTC);
        playListAdapter.setVicTextColor(vicTC);
        playListAdapter.setSelectItemColor(accentC);

        songOption.themeChange(themeEnum, cs);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {
        updatePlayListAdapter();
        listOption.update(obj, completed);
        songOption.update(obj, completed);
    }

    private void updatePlayListAdapter() {
        try {
            Song song = control.currentSong();
            SongInfo info = mediaManager.getSongInfo(song);
            if (info == null) {
                return;
            }

            List<Song> ss = control.getPlayList();
            data.clear();
            for (Song s : ss) {
                data.add(mediaManager.getSongInfo(s));
            }
            playListAdapter.notifyDataSetChanged();

            int i = ss.indexOf(song);
            if (i != -1) {
                playListAdapter.setSelectItem(i);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void updatePlayMode() {
        songOption.updatePlayMode();
    }

    @Override
    public void emptyMediaLibrary() {
        int count = mViewRoot.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mViewRoot.getChildAt(i);
            v.setEnabled(false);
        }
    }

    public void updateFavorite() {
        Log.d("updateCurrentPlay", "play/BottomNavigationController updateFavorite");
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

    // updateColors 在 VARYING 主题时才会不断调用
    public void updateColors(int color, boolean isVarying) {
        Log.d("updateCurrentPlay", "play/BottomNavigationController updateColors");
        int alpha = activity.getResources().getInteger(R.integer.play_list_bg_alpha);
        int colorA = ColorUtils.setAlphaComponent(color, alpha);

        mRealTimeView.setOverlayColor(colorA);
        ColorDrawable bd = new ColorDrawable(color);
        bd.setAlpha(10);
        mListTitleContainer.setBackground(bd);

        ThemeEnum t;
        double d = ColorUtils.calculateLuminance(colorA);
        if (d - 0.400 > 0.000001) {
            t = WHITE;
        } else {
            t = DARK;
        }

        int cs[] = new int[2];
        switch (t) {
            case WHITE: {
                if (isVarying) {
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorWhiteThemeForPlayOptions();
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2WhiteThemeTextColor();
                }
                break;
            }
            case DARK:
            default: {
                if (isVarying) {
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorDarkThemeForPlayOptions();
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2DarkThemeTextColor();
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

}
