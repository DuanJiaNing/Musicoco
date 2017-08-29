package com.duan.musicoco.play.album;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.app.Init;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.image.BitmapProducer;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.ColorUtils;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * 切换歌曲时改变专辑图片（从缓存中获取，没有则生成并添加到缓存）
 * 控制切换动画
 * 播放动画
 */

public final class AlbumPictureController {

    private final ImageSwitcher view;

    private final Context context;

    private final ValueAnimator rotateAnim;

    private boolean isSpin = false;

    private final BitmapCache cache;

    private final BitmapProducer bitmapProducer;

    private int defaultColor = Color.DKGRAY;
    private int defaultTextColor = Color.DKGRAY;
    private int[] colors;
    private final int size;

    public AlbumPictureController(Context context, final ImageSwitcher view, int size) {
        this.view = view;
        this.size = size;
        this.context = context;
        this.cache = new BitmapCache(context, BitmapCache.CACHE_ALBUM_VISUALIZER_IMAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getColor(R.color.default_play_bg_color);
            defaultTextColor = context.getColor(R.color.default_play_text_color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getResources().getColor(R.color.default_play_bg_color, null);
            defaultTextColor = context.getResources().getColor(R.color.default_play_text_color, null);
        } else {
            defaultColor = context.getResources().getColor(R.color.default_play_bg_color);
            defaultTextColor = context.getResources().getColor(R.color.default_play_text_color);
        }

        this.bitmapProducer = new BitmapProducer(context);

        colors = new int[]{
                defaultColor,
                defaultTextColor,
                defaultColor,
                defaultTextColor
        };

        rotateAnim = ObjectAnimator.ofFloat(0, 360);
        rotateAnim.setDuration(45 * 1000);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                view.getCurrentView().setRotation(value);
            }
        });
    }

    /**
     * 切换歌曲的同时返回从歌曲专辑图片中提取出的四种颜色值{@link ColorUtils#get4DarkColorWithTextFormBitmap(Bitmap, int, int, int[])}
     */
    public int[] pre(@NonNull SongInfo song, boolean updateColors) {

        view.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
        view.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right));

        rotateAnim.cancel();
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = bitmapProducer.getBitmapForVisualizer(cache, song.getAlbum_path(), size, defaultColor);
        if (bitmap != null) {
            if (updateColors)
                ColorUtils.get4DarkColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);

            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            try {
                view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
            } catch (Exception e) {
                Bitmap b = Init.initAlbumVisualizerImageCache((Activity) context);
                view.setImageDrawable(new BitmapDrawable(context.getResources(), b));
            }
        }

        if (isSpin()) {
            startSpin();
        }

        return colors;
    }

    public int[] next(@NonNull SongInfo song, boolean updateColors) {

        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_left);
        view.setInAnimation(in);
        view.setOutAnimation(out);

        rotateAnim.cancel();
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = bitmapProducer.getBitmapForVisualizer(cache, song.getAlbum_path(), size, defaultColor);
        if (bitmap != null) {
            if (updateColors) {
                ColorUtils.get4DarkColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);
            }
            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            try {
                view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
            } catch (Exception e) {
                Bitmap b = Init.initAlbumVisualizerImageCache((Activity) context);
                view.setImageDrawable(new BitmapDrawable(context.getResources(), b));
            }
        }

        if (isSpin()) {
            startSpin();
        }

        return colors;
    }

    public void startSpin() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused()) {
                rotateAnim.resume();
            } else {
                rotateAnim.start();
            }
        } else {
            rotateAnim.start();
        }

        isSpin = true;
    }

    public void stopSpin() {

        if (rotateAnim.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                rotateAnim.pause();
            } else {
                rotateAnim.cancel();
            }
            isSpin = false;
        }
    }

    public boolean isSpin() {
        return isSpin;
    }

}
