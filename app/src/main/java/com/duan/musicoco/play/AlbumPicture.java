package com.duan.musicoco.play;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageSwitcher;

import com.duan.musicoco.view.Album;

/**
 * Created by DuanJiaNing on 2017/6/13.
 */

public class AlbumPicture implements Album {

    private ImageSwitcher view;

    private final Context context;

    private Animation rotateAnim;

    public AlbumPicture(Context context, ImageSwitcher view) {
        this.view = view;
        this.context = context;

        rotateAnim = new RotateAnimation(0, 360, view.getWidth() / 2, view.getHeight() / 2);
        rotateAnim.setFillAfter(true);
        rotateAnim.setDuration(1000 * 40);
        rotateAnim.setInterpolator(new LinearInterpolator());
        rotateAnim.setRepeatCount(Animation.INFINITE);
        rotateAnim.setRepeatMode(Animation.RESTART);

    }

    public void pre(@NonNull PictureBuilder pictureBuilder) {
        Bitmap bitmap = pictureBuilder.getBitmap();
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

    public void pre(@NonNull Bitmap bitmap) {
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

    public void next(@NonNull PictureBuilder pictureBuilder) {
        Bitmap bitmap = pictureBuilder.getBitmap();
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

    public void next(@NonNull Bitmap bitmap) {
        view.setImageDrawable(new BitmapDrawable(context.getResources(), bitmap));
    }

    public void setRotateAnim(Animation anim) {
        if (anim == null)
            return;

        this.rotateAnim = anim;
    }

    @Override
    public void startSpin() {
        view.startAnimation(rotateAnim);
    }

    @Override
    public void stopSpin() {
        if (rotateAnim.hasStarted())
            rotateAnim.cancel();
    }

}
