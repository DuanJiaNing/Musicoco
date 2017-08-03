package com.duan.musicoco.play;

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
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
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
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.PlayListAdapter;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealTimeBlurView;

import java.util.Arrays;

import static com.duan.musicoco.preference.ThemeEnum.DARK;
import static com.duan.musicoco.preference.ThemeEnum.WHITE;

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

    private PlayPreference playPreference;
    private AppPreference appPreference;
    private PlayListAdapter playListAdapter;
    private IPlayControl control;
    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;

    private ListOption listOption;
    private SongOption songOption;
    private int currentDrawableColor;
    private SongOperation songOperation;

    public BottomNavigationController(Activity activity, DBMusicocoController dbMusicoco,
                                      MediaManager mediaManager, PlayPreference playPreference,
                                      AppPreference appPreference) {
        this.activity = activity;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.playPreference = playPreference;
        this.appPreference = appPreference;

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
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        themeEnum = appPreference.getTheme();
        int[] cs;
        switch (themeEnum) {
            case DARK: {
                cs = com.duan.musicoco.util.ColorUtils.get10DarkThemeColors(activity);
                break;
            }
            case VARYING:
            case WHITE:
            default:
                cs = com.duan.musicoco.util.ColorUtils.get10WhiteThemeColors(activity);
                break;
        }

        songOption.themeChange(themeEnum, cs);
        listOption.themeChange(themeEnum, cs);
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {
        Log.d("updateCurrentPlay", "play/BottomNavigationController updateCurrentPlay");
        playListAdapter.update(obj, completed);
        listOption.update(obj, completed);
        songOption.update(obj, completed);
    }

    public void updatePlayMode() {
        Log.d("updateCurrentPlay", "play/BottomNavigationController updatePlayMode");
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
            Log.d("updateCurrentPlay", "play/BottomNavigationController updateCurrentSheet");
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
            Log.d("updateCurrentPlay", "play/BottomNavigationController ListOption#updateColors");

            mSheet.setTextColor(currentDrawableColor);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLocation.getDrawable().setTint(currentDrawableColor);
            }
        }

        @Override
        public void themeChange(ThemeEnum themeEnum, int[] colors) {

        }

        @Override
        public void update(Object obj, OnUpdateStatusChanged statusChanged) {
            Log.d("updateCurrentPlay", "play/BottomNavigationController ListOption#updateCurrentPlay");
            updateCurrentSheet();
        }
    }

    private class SongOption implements
            View.OnClickListener,
            OnViewVisibilityChange,
            OnThemeChange,
            AdapterView.OnItemClickListener,
            OnContentUpdate {

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
                ToastUtils.showShortToast(builder.toString());
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
            Log.d("updateCurrentPlay", "play/BottomNavigationController SongOption#updateColors");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hidePlayListBar.getDrawable().setTint(currentDrawableColor);
                playMode.getDrawable().setTint(currentDrawableColor);
                playFavorite.getDrawable().setTint(currentDrawableColor);
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
            Log.d("updateCurrentPlay", "play/BottomNavigationController SongOption#updatePlayMode");

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
            Log.d("updateCurrentPlay", "play/BottomNavigationController SongOption#updateCurrentFavorite");

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

        public void initData() {
            moreOptionsAdapter = new OptionsAdapter(activity);
            mDialog.setAdapter(moreOptionsAdapter);
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
                SongInfo info = mediaManager.getSongInfo(song);
                if (info != null) {
                    switch (position) {
                        case 0: // 收藏到歌单
                            songOperation.handleCollectToSheet(info);
                            break;
                        case 1: // 查看详情
                            songOperation.checkSongDetail(song);
                            break;
                        case 2: //彻底删除
                            songOperation.handleDeleteSongForever(song);
                            break;
                        case 3: {//从歌单中移除(非主歌单才有)
                            songOperation.removeSongFromCurrentPlayingSheet(song);
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

    }
}
