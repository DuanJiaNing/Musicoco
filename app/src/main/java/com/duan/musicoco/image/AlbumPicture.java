package com.duan.musicoco.image;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.cache.BitmapCache;
import com.duan.musicoco.media.SongInfo;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtil;
import com.duan.musicoco.view.Album;

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

    public AlbumPicture(Context context, ImageSwitcher view) {
        this.view = view;
        this.context = context;
        this.cache = new BitmapCache(context);

        Bitmap def = BitmapFactory.decodeResource(context.getResources(), R.mipmap.default_album_pic);
        builder = new PictureBuilder(context, def);

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

    public void pre(@NonNull SongInfo song) {
        Bitmap bitmap = getBitmap(song);
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

    public void next(@NonNull SongInfo song) {
        Bitmap bitmap = getBitmap(song);
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
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

    public Bitmap getBitmap(SongInfo info) {
        if (info == null)
            return builder.getDefaultBitmap();

        String key = StringUtil.stringToMd5(info.getAlbum_path());

        Bitmap result = cache.get(key);

        if (result == null) { //磁盘缓存中没有

            builder.reset();

            int r = Math.min(view.getWidth(), view.getHeight());

            int dc = Color.BLACK;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dc = context.getColor(R.color.colorPrimaryLight);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dc = context.getResources().getColor(R.color.colorPrimaryLight, null);
            } else
                dc = context.getResources().getColor(R.color.colorPrimaryLight);

            PictureBuilder build = builder.setPath(info.getAlbum_path())
                    .resize(r)
                    .toRoundBitmap()
                    .build();

            int[] colors = new int[2];
            ColorUtils.get2ColorFormBitmap(build.getBitmap(), dc, colors);

            int color = dc;
            for (int c : colors)
                if (c != dc) {
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

        return result;
    }
}
