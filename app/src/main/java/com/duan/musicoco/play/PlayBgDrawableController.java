package com.duan.musicoco.play;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.preference.PlayBackgroundModeEnum;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.util.AnimationUtils;

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
                        .load(path)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .bitmapTransform(vtf)
                        .crossFade()
                        .into(new GlideDrawableImageViewTarget((ImageView) isBg.getCurrentView()) {

                            @Override
                            public void setDrawable(Drawable drawable) {
                                isBg.setImageDrawable(drawable);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                Resource<Bitmap> resource = vtf.transform(new Resource<Bitmap>() {
                                    @Override
                                    public Bitmap get() {
                                        Bitmap bitmap = BitmapFactory.decodeResource(
                                                activity.getResources(),
                                                R.drawable.default_album);
                                        return bitmap;
                                    }

                                    @Override
                                    public int getSize() {
                                        Bitmap b = get();
                                        int size;
                                        if (b != null) {
                                            size = b.getRowBytes() * b.getHeight() / 1024;
                                        } else {
                                            size = 0;
                                        }
                                        return size;
                                    }

                                    @Override
                                    public void recycle() {
                                        Bitmap b = get();
                                        if (b != null) {
                                            if (!b.isRecycled()) {
                                                b.recycle();
                                                b = null;
                                            }
                                        }
                                    }
                                }, isBg.getWidth(), isBg.getHeight());
                                Bitmap b = resource.get();
                                if (b != null) {
                                    isBg.setImageDrawable(new BitmapDrawable(activity.getResources(), b));
                                }
                            }

                        });
                break;
            }
            case PICTUREWITHBLUR:
            default: {
                final BlurTransformation btf = new BlurTransformation(activity, 10, 10);
                Glide.with(activity)
                        .load(path)
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .bitmapTransform(btf)
                        .crossFade()
                        .into(new GlideDrawableImageViewTarget((ImageView) isBg.getCurrentView()) {

                            @Override
                            public void setDrawable(Drawable drawable) {
                                isBg.setImageDrawable(drawable);
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                Resource<Bitmap> resource = btf.transform(new Resource<Bitmap>() {
                                    @Override
                                    public Bitmap get() {
                                        Bitmap bitmap = BitmapFactory.decodeResource(
                                                activity.getResources(),
                                                R.drawable.default_album);
                                        return bitmap;
                                    }

                                    @Override
                                    public int getSize() {
                                        Bitmap b = get();
                                        int size;
                                        if (b != null) {
                                            size = b.getRowBytes() * b.getHeight() / 1024;
                                        } else {
                                            size = 0;
                                        }
                                        return size;
                                    }

                                    @Override
                                    public void recycle() {
                                        Bitmap b = get();
                                        if (b != null) {
                                            if (!b.isRecycled()) {
                                                b.recycle();
                                                b = null;
                                            }
                                        }
                                    }
                                }, isBg.getWidth(), isBg.getHeight());
                                Bitmap b = resource.get();
                                if (b != null) {
                                    isBg.setImageDrawable(new BitmapDrawable(activity.getResources(), b));
                                }
                            }

                        });
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
