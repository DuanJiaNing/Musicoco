package com.duan.musicoco.image;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.view.Album;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.duan.musicoco.cache.BitmapCache.DEFAULT_PIC_KEY;

/**
 * Created by DuanJiaNing on 2017/6/13.
 * 切换歌曲时改变专辑图片（从缓存中获取，没有则生成并添加到缓存）
 * 控制切换动画
 * 播放动画
 */

public final class AlbumPicture implements Album {

    private final ImageSwitcher view;

    private final Context context;

    private final ValueAnimator rotateAnim;

    private final ValueAnimator randomAnim;

    private boolean isSpin = false;

    private final PictureBuilder builder;

    private final BitmapCache cache;

    private int ran = 0;

    private int defaultColor = Color.DKGRAY;
    private int defaultTextColor = Color.DKGRAY;
    private int[] colors;

    public AlbumPicture(Context context, final ImageSwitcher view) {
        this.view = view;
        this.context = context;
        this.cache = BitmapCache.getInstance(context);
        this.builder = new PictureBuilder(context);

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

        randomAnim = ObjectAnimator.ofFloat(view, "rotationY", 0, 0);
        randomAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        randomAnim.setDuration(1000);

        //FIXME 移除
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
        }, 2000, 60 * 1000);

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


    private void addDefaultOuters(PictureBuilder builder) {

        if (builder == null || builder.getBitmap() == null)
            return;

        int[] colors = new int[2];
        ColorUtils.get2ColorFormBitmap(builder.getBitmap(), defaultColor, colors);

        int color = defaultColor;
        for (int c : colors)
            if (c != defaultColor) {
                color = c;
                break;
            }

        builder.addOuterCircle(0, 10, color)
                .addOuterCircle(7, 1, Color.WHITE);
    }

    /**
     * 切换歌曲的同时返回从歌曲专辑图片中提取出的四种颜色值{@link ColorUtils#get2ColorWithTextFormBitmap(Bitmap, int, int, int[])}
     */
    public int[] pre(@NonNull SongInfo song, boolean updateColors) {

        view.setInAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left));
        view.setOutAnimation(AnimationUtils.loadAnimation(context, android.R.anim.slide_out_right));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused())
                rotateAnim.cancel();
        }
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = getBitmap(song);
        if (bitmap != null) {
            if (updateColors)
                ColorUtils.get2ColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);

            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
        }

        return colors;
    }

    public int[] next(@NonNull SongInfo song, boolean updateColors) {

        view.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_in_right));
        view.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_out_left));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (rotateAnim.isPaused())
                rotateAnim.cancel();
        }
        view.getNextView().setRotation(0.0f);

        Bitmap bitmap = getBitmap(song);
        if (bitmap != null) {
            if (updateColors)
                ColorUtils.get2ColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, this.colors);

            view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
        } else {
            view.setImageDrawable(new BitmapDrawable(context.getResources(), cache.getDefaultBitmap()));
        }
        return colors;
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
            result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
        else {

            String key = StringUtils.stringToMd5(info.getAlbum_path());

            result = cache.get(key);

            if (result == null) { //磁盘缓存中没有

                builder.reset();

                //使 宽 = 高 = r
                int r = Math.min(view.getWidth(), view.getHeight());

                builder.setPath(info.getAlbum_path())
                        .resize(r)
                        .toRoundBitmap()
                        .build();

                addDefaultOuters(builder);

                Bitmap b = builder.getBitmap();
                if (b != null) { // 成功构建
                    cache.add(key, b);
                    result = b;
                } else //构建失败
                    result = cache.get(StringUtils.stringToMd5(DEFAULT_PIC_KEY));
            }
        }
        return result;
    }
}
