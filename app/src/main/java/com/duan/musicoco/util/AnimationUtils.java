package com.duan.musicoco.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.animation.OvershootInterpolator;

/**
 * Created by DuanJiaNing on 2017/6/22.
 */

public class AnimationUtils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void startColorGradientAnim(int duration, final View view, @Nullable Animator.AnimatorListener listener, int... values) {
        final ValueAnimator anim = ObjectAnimator.ofArgb(values);
        anim.setDuration(duration);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int va = (int) animation.getAnimatedValue();
                view.setBackgroundColor(va);
            }
        });
        if (listener != null) {
            anim.addListener(listener);
        }
        anim.start();
    }

    public static void startTranslateYAnim(float from, float to, int duration, final View view, @Nullable TimeInterpolator interpolator) {
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
        anim.start();
    }

    public static void startAlphaAnim(@NonNull final View view, int duration, @Nullable Animator.AnimatorListener listener, float... values) {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(values);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                view.setAlpha(alpha);
            }
        });
        if (listener != null) {
            alphaAnim.addListener(listener);
        }

        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }

    public static void startScaleAnim(@NonNull View view, int duration, @Nullable Animator.AnimatorListener listener, float... values) {
        ValueAnimator animSX = ObjectAnimator.ofFloat(view, "scaleX", values);
        ValueAnimator animSY = ObjectAnimator.ofFloat(view, "scaleY", values);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);
        set.setInterpolator(new OvershootInterpolator());
        set.play(animSX).with(animSY);
        if (listener != null) {
            set.addListener(listener);
        }
        set.start();
    }
}
