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
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.ExceptionHandler;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnViewVisibilityChange;
import com.duan.musicoco.app.interfaces.OnPlayListVisibilityChange;
import com.duan.musicoco.app.interfaces.UserInterfaceUpdate;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealTimeBlurView;

import static com.duan.musicoco.preference.Theme.DARK;
import static com.duan.musicoco.preference.Theme.WHITE;

/**
 * Created by DuanJiaNing on 2017/7/4.
 */

public class PlayListController implements
        View.OnClickListener,
        OnPlayListVisibilityChange,
        UserInterfaceUpdate,
        OnEmptyMediaLibrary,
        OnThemeChange {

    private Activity activity;
    private boolean isListShowing = false;
    private boolean isListTitleHide = false;

    private CardView mViewRoot;
    private ListView mPlayList;
    private RealTimeBlurView mRealTimeView;
    private View mListTitleContainer;
    private View vDarkBg;
    private LinearLayout llRootMain;

    private PlayListAdapter playListAdapter;
    private PlayPreference playPreference;
    private IPlayControl control;

    private ListOption listOption;
    private SongOption songOption;

    public PlayListController(Activity activity) {
        this.activity = activity;
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
        params.height = metrics.heightPixels / 2;
        mViewRoot.setX(0);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        mViewRoot.setY(metrics.heightPixels - marginB);
        mViewRoot.setLayoutParams(params);

    }

    public void initData(IPlayControl control) {
        this.control = control;

        if (playListAdapter == null) {
            playListAdapter = new PlayListAdapter(activity, control);
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

        themeChange(null, null);
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
            startAlphaAnim(duration, null, 0.0f, 0.6f);
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
                new DecelerateInterpolator(), null);

        songOption.show();
        listOption.hide();

        vDarkBg.setClickable(false);

        if (playPreference.getTheme() == WHITE) {
            startAlphaAnim(duration, new Animator.AnimatorListener() {
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
        } else
            vDarkBg.setVisibility(View.GONE);
    }

    public void hidePlayListTitle() {
        isListTitleHide = true;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        float from = mViewRoot.getY();
        float to = from + marginB;

        startTranslateTitleAnim(from, to, duration);
    }

    public void showPlayListBar() {
        isListTitleHide = false;

        final int duration = activity.getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) activity.getResources().getDimension(R.dimen.action_bar_default_height);
        float from = mViewRoot.getY();
        float to = from - marginB;

        startTranslateTitleAnim(from, to, duration);

    }

    private void startAlphaAnim(int duration, @Nullable Animator.AnimatorListener listener, float... values) {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(values);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                vDarkBg.setAlpha(alpha);
            }
        });
        if (listener != null)
            alphaAnim.addListener(listener);

        alphaAnim.setDuration(duration);
        alphaAnim.start();
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
                if (isListShowing())
                    hide();
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

        if (playListAdapter == null)
            return;

        int color = mRealTimeView.getOverlayColor();
        double d = ColorUtils.calculateLuminance(color);
        Theme t;
        if (d - 0.400 > 0.000001)
            t = WHITE;
        else
            t = DARK;

        playListAdapter.themeChange(t, null);
        listOption.themeChange(t, null);
        songOption.themeChange(t, null);

    }

    @Override
    public void update(Object obj) {
        if (!(obj instanceof Integer)) {
            return;
        }

        int color = (int) obj;
        int alpha = activity.getResources().getInteger(R.integer.play_list_bg_alpha);
        int colorA = ColorUtils.setAlphaComponent(color, alpha);

        mRealTimeView.setOverlayColor(colorA);
        mListTitleContainer.setBackgroundColor(color);
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

    private class ListOption implements
            View.OnClickListener,
            OnViewVisibilityChange,
            OnThemeChange {

        private ViewGroup container;
        private ImageButton location;

        public ListOption(Activity activity) {

            container = (ViewGroup) activity.findViewById(R.id.play_list_show_bar);
            location = (ImageButton) activity.findViewById(R.id.play_location);

            container.setOnClickListener(this);
            location.setOnClickListener(this);
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
                        PlayListController.this.hide();
                    else PlayListController.this.show();
                    break;
            }
        }

        @Override
        public void show() {
            container.setVisibility(View.VISIBLE);
        }

        @Override
        public void hide() {
            container.setVisibility(View.GONE);
        }

        @Override
        public void themeChange(Theme theme, int[] colors) {
            int color;
            switch (theme) {
                case WHITE: {
                    int[] cs = com.duan.musicoco.util.ColorUtils.getWhiteListThemeTextColor(PlayListController.this.activity);
                    color = cs[0];
                    break;
                }
                case DARK:
                default: {
                    int[] cs = com.duan.musicoco.util.ColorUtils.getDarkListThemeTextColor(PlayListController.this.activity);
                    color = cs[0];
                    break;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                location.getDrawable().setTint(color);
            }
        }
    }

    private class SongOption implements
            View.OnClickListener,
            OnViewVisibilityChange,
            OnThemeChange {

        private ViewGroup container;
        private ImageButton hidePlayListBar;
        private ImageButton playMode;

        public SongOption(Activity activity) {

            container = (ViewGroup) activity.findViewById(R.id.play_list_hide_bar);
            hidePlayListBar = (ImageButton) activity.findViewById(R.id.play_list_hide);
            playMode = (ImageButton) activity.findViewById(R.id.play_mode);

            hidePlayListBar.setOnClickListener(this);
            playMode.setOnClickListener(this);
            container.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.play_mode:
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
                        ToastUtils.showToast(activity, builder.toString());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        new ExceptionHandler().handleRemoteException(activity,
                                activity.getString(R.string.exception_remote), null
                        );
                    }
                    break;

                case R.id.play_list_hide_bar:
                    if (isListShowing)
                        PlayListController.this.hide();
                    else PlayListController.this.show();
                    break;
                case R.id.play_list_hide:
                    if (!isListTitleHide)
                        hidePlayListTitle();
                    break;
            }
        }

        @Override
        public void show() {
            container.setVisibility(View.VISIBLE);
        }

        @Override
        public void hide() {
            container.setVisibility(View.GONE);
        }

        @Override
        public void themeChange(Theme theme, int[] colors) {
            int color;
            switch (theme) {
                case WHITE: {
                    int[] cs = com.duan.musicoco.util.ColorUtils.getWhiteListThemeTextColor(PlayListController.this.activity);
                    color = cs[0];
                    break;
                }
                case DARK:
                default: {
                    int[] cs = com.duan.musicoco.util.ColorUtils.getDarkListThemeTextColor(PlayListController.this.activity);
                    color = cs[0];
                    break;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hidePlayListBar.getDrawable().setTint(color);
                playMode.getDrawable().setTint(color);
            }

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
                drawable.setTint(playListAdapter.getColorVic());
            }
            playMode.setImageDrawable(drawable);

            return mode;
        }
    }

}
