package com.duan.musicoco.rmp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.bean.DBSongInfo;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RecentMostPlayActivity extends RootActivity {

    private FirstThreeViewHolder first;
    private FirstThreeViewHolder second;
    private FirstThreeViewHolder third;

    private final List<DataHolder> data = new ArrayList<>();
    private MediaManager mediaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_most_play);

        initViews();

    }

    private void initData() {
        mediaManager = MediaManager.getInstance(this);

        Observable.OnSubscribe<Void> onSubscribe = new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                subscriber.onStart();
                LoadData();
                subscriber.onCompleted();
            }
        };

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        showFirstThree();
                        initList();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }

    private void LoadData() {
        MainSheetHelper helper = new MainSheetHelper(this, dbController);
        List<DBSongInfo> info = helper.getMainSheetSongInfo(MainSheetHelper.SHEET_ALL);
        ArrayList<DBSongInfo> result = DBSongInfo.descSortByPlayTimes(info);

        int count = getResources().getInteger(R.integer.rmp_count);
        count = count > result.size() ? result.size() : count;
        // 左闭右开
        for (DBSongInfo in : result.subList(0, count)) {
            DataHolder da = new DataHolder();
            da.times = in.playTimes;

            SongInfo songInfo = mediaManager.getSongInfo(in.path);
            String title = songInfo.getTitle();
            String arts = songInfo.getArtist();

            da.title = title;
            da.arts = arts;
            data.add(da);
        }

    }

    private void initList() {

    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        initData();
    }

    private void showFirstThree() {
        AnimatorSet fi = getAnimSet(first);
        fi.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                first.setVisible(View.VISIBLE);
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
        AnimatorSet se = getAnimSet(second);
        se.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                second.setVisible(View.VISIBLE);
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
        AnimatorSet th = getAnimSet(third);
        th.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                third.setVisible(View.VISIBLE);
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

        AnimatorSet set = new AnimatorSet();
        int duration = getResources().getInteger(R.integer.anim_default_duration);
        set.setDuration(duration);

        set.play(fi).before(se);
        set.play(se).before(th);
        set.start();
    }

    private AnimatorSet getAnimSet(final FirstThreeViewHolder holder) {
        ValueAnimator transY = ObjectAnimator.ofFloat(100.0f, 0.0f);
        transY.setInterpolator(new OvershootInterpolator());
        transY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                for (View v : holder.getViews()) {
                    v.setTranslationY(va);
                }
            }
        });

        ValueAnimator alpha = ObjectAnimator.ofFloat(0.0f, 1.0f);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                for (View v : holder.getViews()) {
                    v.setAlpha(va);
                }
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.play(transY).with(alpha);

        return set;
    }

    private void initViews() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.rmp_a_toolbar);
        setSupportActionBar(toolbar);
        // FIXME 点击无效 只能在 onOptionsItemSelected 中设置
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        first = new FirstThreeViewHolder(FirstThreeViewHolder.FIRST);
        second = new FirstThreeViewHolder(FirstThreeViewHolder.SECOND);
        third = new FirstThreeViewHolder(FirstThreeViewHolder.THIRD);
        first.setVisible(View.INVISIBLE);
        second.setVisible(View.INVISIBLE);
        third.setVisible(View.INVISIBLE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class FirstThreeViewHolder {
        TextView number;
        ImageView image;
        TextView name;
        TextView arts;
        TextView time;

        final static String FIRST = "first";
        final static String SECOND = "second";
        final static String THIRD = "third";

        FirstThreeViewHolder(String which) {
            if (TextUtils.isEmpty(which) || (!which.equals(FIRST) && !which.equals(SECOND) && !which.equals(THIRD))) {
                return;
            }
            createHolder(which);
        }

        View[] getViews() {
            return new View[]{number, image, name, arts, time};
        }

        void setVisible(int visible) {
            for (View v : getViews()) {
                v.setVisibility(visible);
            }
        }

        void createHolder(String which) {
            String nu = "rmp_a_" + which + "_number";
            String im = "rmp_a_" + which + "_image";
            String na = "rmp_a_" + which + "_name";
            String ar = "rmp_a_" + which + "_arts";
            String ti = "rmp_a_" + which + "_time";

            Class<R.id> idClass = R.id.class;
            try {

                number = (TextView) findViewById(idClass.getField(nu).getInt(R.id.class));
                image = (ImageView) findViewById(idClass.getField(im).getInt(R.id.class));
                name = (TextView) findViewById(idClass.getField(na).getInt(R.id.class));
                arts = (TextView) findViewById(idClass.getField(ar).getInt(R.id.class));
                time = (TextView) findViewById(idClass.getField(ti).getInt(R.id.class));

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    public static class DataHolder {
        String title;
        String arts;
        int times;
    }
}
