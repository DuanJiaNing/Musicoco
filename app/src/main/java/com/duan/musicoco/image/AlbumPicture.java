package com.duan.musicoco.image;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtil;
import com.duan.musicoco.view.Album;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * 切换歌曲时改变专辑图片（从缓存中获取，没有则生成并添加到缓存）
 * 控制切换动画
 * 播放动画
 */

public final class AlbumPicture implements Album {

    private final ImageSwitcher view;

    private final Context context;

    private ValueAnimator rotateAnim;

    private ValueAnimator randomAnim;

    private boolean isSpin = false;

    private final PictureBuilder builder;

    private final BitmapCache cache;

    private int ran = 0;

    private int defaultColor = Color.DKGRAY;
    private int defaultTextColor = Color.DKGRAY;
    private int[] colors;

    public final static String DEFAULT_PIC_KEY = "default_pic_key";

    public AlbumPicture(Context context, ImageSwitcher view) {
        this.view = view;
        this.context = context;
        this.cache = new BitmapCache(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getColor(R.color.colorPrimaryLight);
            defaultTextColor = context.getColor(R.color.colorAccent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            defaultColor = context.getResources().getColor(R.color.colorPrimaryLight, null);
            defaultTextColor = context.getResources().getColor(R.color.colorAccent, null);
        } else {
            defaultColor = context.getResources().getColor(R.color.colorPrimaryLight);
            defaultTextColor = context.getResources().getColor(R.color.colorAccent);
        }

        colors = new int[]{
                defaultColor,
                defaultTextColor,
                defaultColor,
                defaultTextColor
        };

        builder = new PictureBuilder(context);
        int r = Math.min(view.getWidth(), view.getHeight());
        builder.resizeForDefault(r, r, R.mipmap.default_album_pic);
        builder.toRoundBitmap();
        ColorUtils.get2ColorWithTextFormBitmap(builder.getBitmap(), defaultColor, defaultTextColor, colors);
        builder.addOuterCircle(0, 10, colors[0])
                .addOuterCircle(7, 1, Color.WHITE);
        cache.add(StringUtil.stringToMd5(DEFAULT_PIC_KEY), builder.getBitmap());

        rotateAnim = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        rotateAnim.setDuration(45 * 1000);
        rotateAnim.setRepeatMode(ValueAnimator.RESTART);
        rotateAnim.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnim.setInterpolator(new LinearInterpolator());

        randomAnim = ObjectAnimator.ofFloat(view, "rotationY", 0, 0);
        randomAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        randomAnim.setDuration(1000);

        new Timer().schedule(new TimerTask() {
            Random rand = new Random();

            @Override
            public void run() {
                AlbumPicture.this.view.post(new Runnable() {
                    @Override
                    public void run() {
                        if (ran == 0)
                            return;

                        float r = rand.nextInt(ran);
                        float rr = rand.nextBoolean() ? -r : r;

                        randomAnim.setFloatValues(0, rr, 0, -rr / 2, 0);
                        randomAnim.start();
                    }
                });
            }
        }, 2000, 6000);

    }

    public int[] pre(@NonNull SongInfo song) {

//        view.setInAnimation(AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left));
//        view.setOutAnimation(AnimationUtils.loadAnimation(context,android.R.anim.slide_out_right));

        Bitmap bitmap = getBitmap(song);
        if (bitmap != null)
            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        else
            view.setImageResource(R.mipmap.default_pic);

        return colors;
    }

    public int[] next(@NonNull SongInfo song) {

//        view.setInAnimation(AnimationUtils.loadAnimation(context,R.anim.slide_in_right));
//        view.setOutAnimation(AnimationUtils.loadAnimation(context,R.anim.slide_out_left));

        Bitmap bitmap = getBitmap(song);
        if (bitmap != null)
            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        else
            view.setImageResource(R.mipmap.default_pic);

        return colors;
    }

    public void setRotateAnim(ValueAnimator anim) {
        if (anim == null)
            return;
        this.rotateAnim = anim;
    }

    @Override
    public void startSpin() {

        ran = 30;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused())
                rotateAnim.resume();
            else rotateAnim.start();
        } else rotateAnim.start();

        isSpin = true;
    }

    @Override
    public void stopSpin() {

        ran = 0;

        if (rotateAnim.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                rotateAnim.pause();
            } else rotateAnim.cancel();
            isSpin = false;
        }
    }

    public boolean isSpin() {
        return isSpin;
    }

    @Nullable
    public Bitmap getBitmap(SongInfo info) {

        Bitmap result;

        if (info == null || info.getAlbum_path() == null)
            result = cache.get(DEFAULT_PIC_KEY);
        else {

            String key = StringUtil.stringToMd5(info.getAlbum_path());

            result = cache.get(key);

            if (result == null) { //磁盘缓存中没有

                builder.reset();

                int r = Math.min(view.getWidth(), view.getHeight());

                PictureBuilder build = builder.setPath(info.getAlbum_path())
                        .resize(r)
                        .toRoundBitmap()
                        .build();

                int[] colors = new int[2];
                ColorUtils.get2ColorFormBitmap(build.getBitmap(), defaultColor, colors);

                int color = defaultColor;
                for (int c : colors)
                    if (c != defaultColor) {
                        color = c;
                        break;
                    }

                Bitmap b = build.addOuterCircle(0, 10, color)
                        .addOuterCircle(7, 1, Color.WHITE)
                        .getBitmap();

                if (b != null) { // 成功构建
                    cache.add(key, b);
                    result = b;
                } else //构建失败
                    result = builder.getDefaultBitmap();
            }
        }

        if (result != null)
            ColorUtils.get2ColorWithTextFormBitmap(result, defaultColor, defaultTextColor, this.colors);

        return result;
    }
}
