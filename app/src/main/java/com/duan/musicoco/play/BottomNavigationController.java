package com.duan.musicoco.play;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
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
import com.duan.musicoco.db.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.shared.ExceptionHandler;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnPlayListVisibilityChange;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.OnViewVisibilityChange;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.PlayListAdapter;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealTimeBlurView;

import static com.duan.musicoco.preference.Theme.DARK;
import static com.duan.musicoco.preference.Theme.WHITE;

/**
 * Created by DuanJiaNing on 2017/7/4.
 */

public class BottomNavigationController implements
        View.OnClickListener,
        OnPlayListVisibilityChange,
        OnContentUpdate,
        OnEmptyMediaLibrary,
        OnThemeChange {

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

    private PlayListAdapter playListAdapter;
    private PlayPreference playPreference;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;

    private ListOption listOption;
    private SongOption songOption;
    private int currentDrawableColor;
    private SongOperation songOperation;

    public BottomNavigationController(Activity activity, DBMusicocoController dbMusicoco, MediaManager mediaManager) {
        this.activity = activity;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.playPreference = new PlayPreference(activity);

        mViewRoot = (CardView) activity.findViewById(R.id.play_list);
        mPlayList = (ListView) activity.findViewById(R.id.play_play_list);

        mRealTimeView = (RealTimeBlurView) activity.findViewById(R.id.play_blur);
        mListTitleContainer = activity.findViewById(R.id.play_list_bar_container);
        vDarkBg = activity.findViewById(R.id.play_dark_bg);
        llRootMain = (LinearLayout) activity.findViewById(R.id.play_root_main);

        listOption = new ListOption(activity);
        songOption = new SongOption(activity);

        vDarkBg.setOnClickListener(this);

        ViewGroup.LayoutParams params = mViewRoot.getLayoutParams();
        DisplayMetrics metrics = Utils.getMetrics(activity);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = metrics.heightPixels * 5 / 9;
        mViewRoot.setX(0);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        mViewRoot.setY(metrics.heightPixels - marginB);
        mViewRoot.setLayoutParams(params);

        //TODO 下拉时隐藏
        mPlayListContainer = (LinearLayout) activity.findViewById(R.id.play_nav_container);

    }

    public void initData(IPlayControl control) {
        this.control = control;
        songOption.initData();
        songOperation = new SongOperation(activity, control, dbMusicoco);

        if (playListAdapter == null) {
            playListAdapter = new PlayListAdapter(
                    activity,
                    control,
                    dbMusicoco,
                    songOperation);
            mPlayList.setAdapter(playListAdapter);
        }

        //更新播放列表字体颜色模式（亮 暗）
        Theme theme = playPreference.getTheme();
        int alpha = activity.getResources().getInteger(R.integer.play_list_bg_alpha);
        int color;
        switch (theme) {
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

        themeChange(theme, null);
        songOption.updatePlayMode();
        songOption.show();
        listOption.hide();

        int count = mViewRoot.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mViewRoot.getChildAt(i);
            v.setEnabled(true);
        }
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
                if (isListShowing()) {
                    hide();
                }
                break;
        }
    }

    public boolean isListTitleHide() {
        return isListTitleHide;
    }

    public boolean isListShowing() {
        return isListShowing;
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        switch (theme) {
            case DARK: {
                updateColors(com.duan.musicoco.util.ColorUtils.get4DarkThemeColors(activity)[2], false);
                break;
            }
            case VARYING:
            case WHITE:
            default:
                updateColors(com.duan.musicoco.util.ColorUtils.get4WhiteThemeColors(activity)[2], false);
                break;
        }

        songOption.themeChange(theme, null);
        listOption.themeChange(theme, null);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {
        playListAdapter.update(obj, completed);
        listOption.update(obj, completed);
    }

    public void updatePlayMode() {
        if (songOption != null) {
            songOption.updatePlayMode();
        }
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

    public void updateColors(int color, boolean isVarying) {
        int alpha = activity.getResources().getInteger(R.integer.play_list_bg_alpha);
        int colorA = ColorUtils.setAlphaComponent(color, alpha);

        mRealTimeView.setOverlayColor(colorA);
        mListTitleContainer.setBackgroundColor(color);

        Theme t = WHITE;
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
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorWhiteThemeForPlayOptions(BottomNavigationController.this.activity);
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2WhiteThemeTextColor(BottomNavigationController.this.activity);
                }
                break;
            }
            case DARK:
            default: {
                if (isVarying) {
                    cs = com.duan.musicoco.util.ColorUtils.get2ColorDarkThemeForPlayOptions(BottomNavigationController.this.activity);
                } else {
                    cs = com.duan.musicoco.util.ColorUtils.get2DarkThemeTextColor(BottomNavigationController.this.activity);
                }
                break;
            }
        }
        currentDrawableColor = cs[0];
        if (playListAdapter != null) {
            playListAdapter.updateColors(null, cs);
        }

        listOption.updateColors();
        songOption.updateColors();

    }

    private class ListOption implements
            View.OnClickListener,
            OnViewVisibilityChange,
            OnThemeChange,
            OnContentUpdate {

        private ViewGroup container;
        private ImageButton mLocation;
        private TextView mSheet;

        public ListOption(Activity activity) {

            container = (ViewGroup) activity.findViewById(R.id.play_list_show_bar);
            mLocation = (ImageButton) activity.findViewById(R.id.play_location);
            mSheet = (TextView) activity.findViewById(R.id.play_sheet);

            container.setOnClickListener(this);
            mLocation.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_location:
                    try {
                        int index = control.currentSongIndex();
                        mPlayList.smoothScrollToPosition(index);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        new ExceptionHandler().handleRemoteException(activity,
                                activity.getString(R.string.exception_remote), null
                        );
                    }
                    break;
                case R.id.play_list_show_bar:
                    if (isListShowing)
                        BottomNavigationController.this.hide();
                    else BottomNavigationController.this.show();
                    break;
            }
        }

        private void updateCurrentSheet() {
            try {
                int id = control.getPlayListId();
                if (id < 0) {
                    String name = MainSheetHelper.getMainSheetName(activity, id);
                    mSheet.setText(name);
                } else {
                    Sheet sheet = dbMusicoco.getSheet(id);
                    String name = "歌单：" + sheet.name + " (" + sheet.count + "首)";
                    mSheet.setText(name);
                }

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

        public void updateColors() {

            mSheet.setTextColor(currentDrawableColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLocation.getDrawable().setTint(currentDrawableColor);
            }
        }

        @Override
        public void themeChange(Theme theme, int[] colors) {

        }

        @Override
        public void update(Object obj, OnUpdateStatusChanged statusChanged) {
            updateCurrentSheet();
        }
    }

    private class SongOption implements
            View.OnClickListener,
            OnViewVisibilityChange,
            OnThemeChange,
            AdapterView.OnItemClickListener {

        private ViewGroup container;
        private final OptionsDialog mDialog;

        private ImageButton hidePlayListBar;
        private ImageButton playMode;
        private ImageButton playFavorite;
        private ImageButton playShowList;
        private ImageButton playShowMore;

        private OptionsAdapter moreOptionsAdapter;

        public SongOption(final Activity activity) {

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

            this.mDialog = new OptionsDialog(activity);
            mDialog.setOnItemClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_mode:
                    handlePlayModeChange();
                    break;
                case R.id.play_list_hide_bar:
                    if (isListShowing) {
                        BottomNavigationController.this.hide();
                    } else {
                        BottomNavigationController.this.show();
                    }
                    break;
                case R.id.play_list_hide:
                    if (!isListTitleHide) {
                        hidePlayListTitle();
                    }
                    break;
                case R.id.play_favorite:
                    handleFavoriteStatusChange();
                    break;
                case R.id.play_show_list:
                    if (isListShowing()) {
                        BottomNavigationController.this.hide();
                    } else {
                        BottomNavigationController.this.show();
                    }
                    break;
                case R.id.play_show_more:
                    handleShowMore();
                    break;
            }
        }

        private void handleShowMore() {
            if (mDialog.isShowing()) {
                mDialog.hide();
            } else {
                Song s = null;
                try {
                    s = control.currentSong();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                SongInfo info = mediaManager.getSongInfo(s);
                String title = activity.getString(R.string.song) + ": " + info.getTitle();
                mDialog.setTitle(title);
                mDialog.show();
            }
        }

        private void handleFavoriteStatusChange() {
            try {
                Song song = control.currentSong();
                if (song != null) {
                    DBSongInfo info = dbMusicoco.getSongInfo(song);
                    boolean isFavorite = info.favorite;
                    boolean reverse = !isFavorite;
                    updateCurrentFavorite(reverse, true);

                    dbMusicoco.updateSongFavorite(song, reverse);
                    //广播通知 MainActivity 更新 MainSheetsController
                    BroadcastManager.sendMyBroadcast(activity, BroadcastManager.FILTER_MAIN_SHEET_CHANGED);

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
                ToastUtils.showShortToast(activity, builder.toString());
            } catch (RemoteException e) {
                e.printStackTrace();
                new ExceptionHandler().handleRemoteException(activity,
                        activity.getString(R.string.exception_remote), null
                );
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

        public void updateColors() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hidePlayListBar.getDrawable().setTint(currentDrawableColor);
                playMode.getDrawable().setTint(currentDrawableColor);
                playFavorite.getDrawable().setTint(currentDrawableColor);
                playShowList.getDrawable().setTint(currentDrawableColor);
                playShowMore.getDrawable().setTint(currentDrawableColor);
            }
        }

        @Override
        public void themeChange(Theme theme, int[] colors) {
            int[] cs;
            switch (theme) {
                case DARK: {
                    cs = com.duan.musicoco.util.ColorUtils.get4DarkThemeColors(activity);
                    break;
                }
                case VARYING:
                case WHITE:
                default:
                    cs = com.duan.musicoco.util.ColorUtils.get4WhiteThemeColors(activity);
                    break;
            }

            int mainBC = cs[0];
            int mainTC = cs[1];
            int vicBC = cs[2];
            int vicTC = cs[3];

            mDialog.setTitleBarBgColor(vicBC);
            mDialog.setContentBgColor(mainBC);
            mDialog.setDivideColor(vicTC);
            mDialog.setTitleTextColor(mainTC);

            moreOptionsAdapter.setTextColor(mainTC);
            moreOptionsAdapter.setIconColor(vicTC);
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

        public int getCurrentDrawableColor() {
            return currentDrawableColor;
        }

        private void updateCurrentFavorite(boolean favorite, boolean useAnim) {

            int color = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                color = activity.getColor(R.color.favorite);
            } else {
                color = activity.getResources().getColor(R.color.favorite);
            }

            int from = getCurrentDrawableColor();
            int to = color;
            if (!favorite) {
                int temp = from;
                from = to;
                to = temp;
            }

            if (useAnim) {
                startFavoriteSwitchAnim(from, to);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    playFavorite.getDrawable().setTint(to);
                }
            }
        }

        private void startFavoriteSwitchAnim(int colorFrom, int colorTo) {
            float sFrom = 1.2f;
            float sCenter = 1.5f;
            float sTo = 1.2f;
            ValueAnimator scaleAnimX = ObjectAnimator.ofFloat(playFavorite, "scaleX", sFrom, sCenter, sTo);
            ValueAnimator scaleAnimY = ObjectAnimator.ofFloat(playFavorite, "scaleY", sFrom, sCenter, sTo);

            ValueAnimator colorAnim = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorAnim = ObjectAnimator.ofArgb(colorFrom, colorTo);
                colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int value = (int) animation.getAnimatedValue();
                        playFavorite.getDrawable().setTint(value);
                    }
                });
            }

            AnimatorSet set = new AnimatorSet();
            set.setDuration(500);
            if (colorAnim != null) {
                set.play(scaleAnimX).with(scaleAnimY).with(colorAnim);
                set.start();
            } else {
                set.play(scaleAnimX).with(scaleAnimY);
                set.start();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    playFavorite.getDrawable().setTint(colorTo);
                }
            }
        }

        public void initData() {
            moreOptionsAdapter = new OptionsAdapter(activity, getIconsID(), getTexts(), null, null);
            mDialog.setAdapter(moreOptionsAdapter);
        }

        private String[] getTexts() {
            String[] sts = activity.getResources().getStringArray(R.array.song_options_titles);
            return sts;
        }

        private int[] getIconsID() {
            int[] ids = new int[4];
            ids[0] = R.drawable.ic_create_new_folder_black_24dp;
            ids[1] = R.drawable.ic_art_track_black_24dp;
            ids[2] = R.drawable.ic_clear_black_24dp;
            ids[3] = R.drawable.ic_delete_forever_black_24dp;

            return ids;
        }

        //歌曲更多操作底部弹出对话框内项目的点击事件
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            try {
                Song song = control.currentSong();
                SongInfo info = mediaManager.getSongInfo(song);
                if (info != null) {
                    switch (position) {
                        case 0:
                            songOperation.handleCollectToSheet(info);
                            break;
                        case 1:
                            songOperation.checkSongDetail(song);
                            break;
                        case 2: //从歌单中移除
                            songOperation.removeSongFromSheet(song);
                            break;
                        case 3: {//彻底删除
                            songOperation.deleteSongFromDisk(song);
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
    }
}
