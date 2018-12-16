package com.duan.musicoco.main.leftnav.play;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.preference.PlayBackgroundModeEnum;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

public class PlayThemeCustomActivity extends RootActivity implements
        ThemeChangeable, View.OnClickListener {

    private int select;

    private ViewHolder viewHolder;
    private DataHolder dataHolder;

    private int mainTC, selectTC, selectBC;

    private boolean isOptionsShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_theme_custom);

        dataHolder = new DataHolder();
        initViews();
        themeChange(null, null);
        initData();
    }

    private void initData() {
        Object theme = playPreference.getTheme();
        if (theme.toString().equals(ThemeEnum.VARYING.toString())) {
            theme = playPreference.getPlayBgMode();
        }

        select = dataHolder.indexOfThemes(theme);
        update();
    }

    private void update() {
        viewHolder.imageSwitch.setImageResource(dataHolder.images[select]);
        for (int i = 0; i < viewHolder.texts.length; i++) {
            TextView view = viewHolder.texts[i];
            if (i == select) {
                view.setTextColor(selectTC);
                view.setBackgroundColor(selectBC);
            } else {
                view.setTextColor(mainTC);
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    private void initViews() {
        viewHolder = new ViewHolder();
        for (View view : viewHolder.texts) {
            view.setOnClickListener(this);
        }
        viewHolder.imageSwitch.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView image = new ImageView(PlayThemeCustomActivity.this);
                image.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return image;
            }
        });

        viewHolder.imageSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOptionsVisible(!isOptionsShow);
            }
        });
        viewHolder.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSave();
            }
        });

    }

    private void handleSave() {
        Object theme = dataHolder.themes[select];
        if (theme instanceof ThemeEnum) {
            playPreference.updateTheme((ThemeEnum) theme);
        } else {
            playPreference.updateTheme(ThemeEnum.VARYING);
            playPreference.updatePlayBgMode((PlayBackgroundModeEnum) theme);
        }

        String msg = getString(R.string.success_modify_saved);
        ToastUtils.showShortToast(msg, this);

        // 播放界面更新主题（底部弹出的选项框）
        BroadcastManager manager = BroadcastManager.getInstance();
        Bundle bundle = new Bundle();
        bundle.putInt(BroadcastManager.Play.PLAY_THEME_CHANGE_TOKEN, BroadcastManager.Play.PLAY_PLAY_THEME_CHANGE);
        manager.sendBroadcast(this, BroadcastManager.FILTER_PLAY_UI_MODE_CHANGE, bundle);

        finish();
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        viewHolder.container.setVisibility(View.VISIBLE);
        viewHolder.done.setVisibility(View.VISIBLE);
        setOptionsVisible(true);
    }

    private void setOptionsVisible(boolean visible) {

        isOptionsShow = visible;

        float y1 = -viewHolder.container.getHeight();
        float y2 = 0;

        View v = viewHolder.done;
        ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        int sh = Utils.getMetrics(this).heightPixels;
        float y3 = sh + v.getHeight() + mp.bottomMargin;
        float y4 = sh - v.getHeight() - mp.bottomMargin;

        float from, to;
        float from1, to1;
        if (visible) {
            from = y1;
            to = y2;

            from1 = y3;
            to1 = y4;
        } else {
            from = y2;
            to = y1;

            from1 = y4;
            to1 = y3;
        }

        int dur = 1000;
        AnimationUtils.startTranslateYAnim(from, to, dur, viewHolder.container, new DecelerateInterpolator());
        AnimationUtils.startTranslateYAnim(from1, to1, dur, v, new DecelerateInterpolator());


    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        int[] cs = ColorUtils.get10ThemeColors(this, appPreference.getTheme());

        mainTC = cs[5];
        selectBC = cs[1];
        selectTC = cs[8];

        int color = android.support.v4.graphics.ColorUtils.setAlphaComponent(cs[3], 150);
        viewHolder.container.setBackgroundColor(color);

        viewHolder.done.setBackgroundTintList(ColorStateList.valueOf(cs[3]));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            viewHolder.done.getDrawable().setTint(mainTC);
        }
        viewHolder.done.setRippleColor(cs[6]);

    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            String desc = ((TextView) v).getText().toString();
            select = dataHolder.indexOf(desc);
            update();
        }
    }

    private class ViewHolder {
        final ImageSwitcher imageSwitch;
        final View container;
        final TextView dark;
        final TextView white;
        final TextView blur;
        final TextView color;
        final TextView gradient;
        final TextView mask;
        final FloatingActionButton done;
        final TextView[] texts;

        public ViewHolder() {
            imageSwitch = (ImageSwitcher) findViewById(R.id.play_theme_custom_image_switch);
            container = findViewById(R.id.play_theme_custom_container);
            dark = (TextView) findViewById(R.id.play_theme_custom_dark);
            white = (TextView) findViewById(R.id.play_theme_custom_white);
            blur = (TextView) findViewById(R.id.play_theme_custom_blur);
            color = (TextView) findViewById(R.id.play_theme_custom_color);
            gradient = (TextView) findViewById(R.id.play_theme_custom_gradient);
            mask = (TextView) findViewById(R.id.play_theme_custom_mask);
            done = (FloatingActionButton) findViewById(R.id.play_theme_custom_done);

            texts = new TextView[]{
                    dark,
                    white,
                    blur,
                    color,
                    gradient,
                    mask
            };

        }
    }

    private class DataHolder {

        private final Object[] themes = {
                ThemeEnum.DARK, // 夜间
                ThemeEnum.WHITE, //白天
                PlayBackgroundModeEnum.PICTUREWITHBLUR, //虚化
                PlayBackgroundModeEnum.COLOR, // 纯色
                PlayBackgroundModeEnum.GRADIENT_COLOR, // 渐变
                PlayBackgroundModeEnum.PICTUREWITHMASK, // 遮罩
        };
        private final int[] images = {
                R.mipmap.dark,
                R.mipmap.white,
                R.mipmap.varying_blur,
                R.mipmap.varying_color,
                R.mipmap.varying_gradient,
                R.mipmap.varying_mask
        };
        private final String[] desc = {
                getString(R.string.play_theme_custom_dark),
                getString(R.string.play_theme_custom_white),
                getString(R.string.play_theme_custom_blur),
                getString(R.string.play_theme_custom_color),
                getString(R.string.play_theme_custom_gradient),
                getString(R.string.play_theme_custom_mask),
        };

        int indexOfThemes(Object obj) {
            for (int i = 0; i < themes.length; i++) {
                if (themes[i].toString().equals(obj.toString())) {
                    return i;
                }
            }
            return -1;
        }

        int indexOf(int item) {
            for (int i = 0; i < images.length; i++) {
                if (images[i] == item) {
                    return i;
                }
            }
            return -1;
        }

        int indexOf(String item) {
            for (int i = 0; i < desc.length; i++) {
                if (desc[i].equals(item)) {
                    return i;
                }
            }
            return -1;
        }

    }
}
