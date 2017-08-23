package com.duan.musicoco.app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.ActivityManager;

/**
 * Created by DuanJiaNing on 2017/8/21.
 */

public class SplashActivity extends InspectActivity {

    private TextHolder tvs;
    private int index;
    private boolean animComplete;
    private boolean initComplete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);

        //权限检查完成后回调 permissionGranted 或 permissionDenied
        checkPermission();

    }

    @Override
    public void permissionGranted(int requestCode) {
        initViews();
        initDataAndStartService();
    }

    private void initViews() {
        View v = findViewById(R.id.splash_container);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{
                        getResources().getColor(R.color.colorPrimary),
                        getResources().getColor(R.color.colorPrimaryDark)
                });
        v.setBackground(gd);

        tvs = new TextHolder();
        tvs.ts[0].post(new Runnable() {
            @Override
            public void run() {
                startTextInAnim(tvs.ts[index]);
            }
        });
    }

    private void startTextInAnim(final TextView t) {
        ValueAnimator anim = ObjectAnimator.ofFloat(t, "translationY", t.getHeight(), 0);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        ValueAnimator alpha = ObjectAnimator.ofFloat(t, "alpha", 0.0f, 1.0f);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(350);
        set.play(anim).with(alpha);

        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                t.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (index != tvs.ts.length - 1) {
                    startTextInAnim(tvs.ts[++index]);
                } else {
                    startFinalAnim();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    private void startFinalAnim() {
        final ImageView image = (ImageView) findViewById(R.id.splash_logo);
        final TextView name = (TextView) findViewById(R.id.splash_name);

        ValueAnimator alpha = ObjectAnimator.ofFloat(image, "alpha", 0.0f, 1.0f);
        alpha.setDuration(1000);
        ValueAnimator alphaN = ObjectAnimator.ofFloat(name, "alpha", 0.0f, 1.0f);
        alphaN.setDuration(1000);
        ValueAnimator tranY = ObjectAnimator.ofFloat(image, "translationY", -image.getHeight() / 3, 0);
        tranY.setDuration(1000);
        ValueAnimator wait = ObjectAnimator.ofInt(0, 100);
        wait.setDuration(1000);
        wait.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (initComplete) {
                    startMainActivity();
                } else {
                    animComplete = true;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                image.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.play(alpha).with(alphaN).with(tranY).before(wait);
        set.start();
    }

    private void initDataAndStartService() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                // FIXME 耗时
                startService();
                // FIXME 耗时
                prepareData();
                // FIXME 耗时 !!
                initAppDataIfNeed();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (animComplete) {
                    startMainActivity();
                } else {
                    initComplete = true;
                }
            }
        }.execute();

    }

    private void startMainActivity() {
        finish();
        ActivityManager.getInstance(SplashActivity.this).startMainActivity();
    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    private class TextHolder {
        TextView[] ts;

        public TextHolder() {
            ts = new TextView[]{
                    (TextView) findViewById(R.id.splash_m),
                    (TextView) findViewById(R.id.splash_u),
                    (TextView) findViewById(R.id.splash_s),
                    (TextView) findViewById(R.id.splash_i),
                    (TextView) findViewById(R.id.splash_c),
                    (TextView) findViewById(R.id.splash_o),
                    (TextView) findViewById(R.id.splash_c1),
                    (TextView) findViewById(R.id.splash_o1)
            };
        }
    }

}
