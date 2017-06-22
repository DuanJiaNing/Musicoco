package com.duan.musicoco.util;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
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

}
