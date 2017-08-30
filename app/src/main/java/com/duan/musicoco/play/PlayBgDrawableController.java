package com.duan.musicoco.play;

import android.app.Activity;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.PlayBackgroundModeEnum;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.StringUtils;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

/**
 * Created by DuanJiaNing on 2017/7/31.
 */

public class PlayBgDrawableController {

    private Activity activity;
    private PlayPreference playPreference;

    private ImageSwitcher isBg;
    private FrameLayout flRootView;


    public PlayBgDrawableController(Activity activity, PlayPreference playPreference) {
        this.activity = activity;
        this.playPreference = playPreference;
    }

    public void initViews() {
        flRootView = (FrameLayout) activity.findViewById(R.id.play_root);
        isBg = (ImageSwitcher) activity.findViewById(R.id.play_bg);

        isBg.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView view = new ImageView(activity);
                view.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return view;
            }
        });
    }

    public void initData() {
    }

    private void updateBackgroundDrawable(PlayBackgroundModeEnum bgMode, SongInfo info) {

        String path = null;
        if (info != null) {
            path = info.getAlbum_path();

        }
        ImageView view = (ImageView) isBg.getCurrentView();
        switch (bgMode) {
            case PICTUREWITHMASK: {
                final VignetteFilterTransformation vtf = new VignetteFilterTransformation(
                        activity,
                        new PointF(0.5f, 0.4f),
                        new float[]{0.0f, 0.0f, 0.0f},
                        0.1f,
                        0.75f
                );

                Glide.with(activity)
                        .load(StringUtils.isReal(path) ? path : R.drawable.default_album)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .bitmapTransform(vtf)
                        .crossFade()
                        .into(view);
                break;
            }
            case PICTUREWITHBLUR:
            default: {
                final BlurTransformation btf = new BlurTransformation(activity, 10, 10);
                Glide.with(activity)
                        .load(StringUtils.isReal(path) ? path : R.drawable.default_album)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .bitmapTransform(btf)
                        .crossFade()
                        .into(view);
                break;
            }
        }
    }

    public void initBackgroundColor(int color) {
        isBg.setVisibility(View.GONE);
        flRootView.setBackgroundColor(color);
    }

    public void updateBackground(int mainBC, int vicBC, SongInfo info) {

        PlayBackgroundModeEnum bgMode = playPreference.getPlayBgMode();
        switch (bgMode) {
            case GRADIENT_COLOR: {

                isBg.setVisibility(View.GONE);

                GradientDrawable drawable = new GradientDrawable(
                        GradientDrawable.Orientation.TR_BL,
                        new int[]{mainBC, vicBC}
                );
                flRootView.setBackground(drawable);

                break;
            }
            case COLOR: {

                isBg.setVisibility(View.GONE);
                ColorDrawable cd = (ColorDrawable) flRootView.getBackground();
                if (cd != null) {
                    if (cd.getColor() != mainBC) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            AnimationUtils.startColorGradientAnim(1000, flRootView, null, cd.getColor(), mainBC);
                        } else {
                            flRootView.setBackgroundColor(mainBC);
                        }
                    }
                } else {
                    flRootView.setBackgroundColor(mainBC);
                }

                break;
            }
            default: {
                if (info == null) {
                    return;
                }

                isBg.setVisibility(View.VISIBLE);
                updateBackgroundDrawable(bgMode, info);
                break;
            }
        }
    }


}
