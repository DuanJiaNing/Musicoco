package com.duan.musicoco.util;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;

/**
 * Created by DuanJiaNing on 2017/6/22.
 */

public class AnimationUtils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void startColorGradientAnim(int duration, final View view, int... values) {
        final ValueAnimator anim = ObjectAnimator.ofArgb(values);
        anim.setDuration(duration);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int va = (int) animation.getAnimatedValue();
                view.setBackgroundColor(va);
            }
        });
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
}
