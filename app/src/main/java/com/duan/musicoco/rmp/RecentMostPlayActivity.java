package com.duan.musicoco.rmp;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.main.MainActivity;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.OptionsAdapter;
import com.duan.musicoco.shared.OptionsDialog;
import com.duan.musicoco.shared.SongOperation;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RecentMostPlayActivity extends RootActivity implements
        ThemeChangeable,
        RMPAdapter.OnItemClickListener {

    private FirstThreeViewHolder first;
    private FirstThreeViewHolder second;
    private FirstThreeViewHolder third;

    private final List<RMPAdapter.DataHolder> data = new ArrayList<>();
    private RMPAdapter adapter;
    private RecyclerView list;
    private Toolbar toolbar;

    private MediaManager mediaManager;
    private ActivityManager activityManager;

    private View line;
    private TextView title;
    private boolean isActivityFirstCreate = true;

    private IPlayControl control;

    private OptionsDialog optionsDialog;
    private OptionsAdapter optionsAdapter;
    private SongOperation songOperation;

    private SongInfo currentClickItem;
    private boolean currentClickItemFavorite;
    private static final int SONG_OPTIONS_FAVORITE = 937;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_most_play);

        mediaManager = MediaManager.getInstance();
        activityManager = ActivityManager.getInstance();
        control = MainActivity.getControl();
        songOperation = new SongOperation(this, control, dbController);

        initViews();

    }

    private void initData() {
        optionsAdapter = new OptionsAdapter(this);
        optionsDialog = new OptionsDialog(this);
        optionsDialog.setAdapter(optionsAdapter);
        initDialogOptions();

        initTitle();
        themeChange(null, null);

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
                        prepareDataForFirstThree();
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

    private void initDialogOptions() {

        // 播放
        optionsAdapter.addOption(
                getString(R.string.play),
                null,
                0,
                R.drawable.ic_play_arrow_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        Song song = new Song(currentClickItem.getData());
                        songOperation.playSongAtSheetAll(song);
                        optionsDialog.hide();
                        finish();
                        activityManager.startPlayActivity(RecentMostPlayActivity.this);

                    }
                });

        // 在歌单中显示
        optionsAdapter.addOption(
                getString(R.string.info_show_in_sheet),
                null,
                1,
                R.drawable.ic_location_searching_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        Song song = new Song(currentClickItem.getData());
                        optionsDialog.hide();
                        finish();
                        activityManager.startSheetDetailActivity(RecentMostPlayActivity.this, MainSheetHelper.SHEET_ALL, song);

                    }
                });

        // 歌曲详情
        optionsAdapter.addOption(
                getString(R.string.song_operation_detail),
                null,
                2,
                R.drawable.ic_art_track_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        Song song = new Song(currentClickItem.getData());
                        optionsDialog.hide();
                        activityManager.startSongDetailActivity(RecentMostPlayActivity.this, song, false);
                    }
                });

        //收藏
        optionsAdapter.addOption(
                getString(R.string.favorite),
                null,
                SONG_OPTIONS_FAVORITE,
                R.drawable.ic_favorite_border,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        Song song = new Song(currentClickItem.getData());
                        optionsDialog.hide();
                        songOperation.reverseSongFavoriteStatus(song);
                    }
                });

        //添加到歌单
        optionsAdapter.addOption(
                getString(R.string.title_add_to_sheet),
                null,
                3,
                R.drawable.ic_create_new_folder_black_24dp,
                new OptionsAdapter.OptionClickListener() {
                    @Override
                    public void onClick(OptionsAdapter.ViewHolder holder, int position, OptionsAdapter.Option option) {
                        optionsDialog.hide();
                        songOperation.handleAddSongToSheet(currentClickItem);
                    }
                });

    }

    private void prepareDataForFirstThree() {

        int c = 3;
        int a = 0;
        FirstThreeViewHolder[] holders = {first, second, third};
        while (data.size() >= c) {
            RMPAdapter.DataHolder d = data.remove(0);

            // 用于点击事件构造歌曲
            holders[a].image.setTag(R.id.rmp_first_three_song_path, d.path);

            holders[a].name.setText(d.title);
            holders[a].arts.setText(d.arts);
            holders[a].time.setText(String.format("%d " + getString(R.string.count), d.times));

            Glide.with(this)
                    .load(d.album)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .placeholder(R.drawable.default_song)
                    .crossFade()
                    .into(holders[a].image);

            a++;
            c--;

            if (c == 0) {
                break;
            }
        }

    }

    private void initTitle() {
        int count = getResources().getInteger(R.integer.rmp_count);
        String str = getString(R.string.replace_rmp_title);
        str = str.replace("*", String.valueOf(count));
        title.setText(str);
    }

    private void LoadData() {
        MainSheetHelper helper = new MainSheetHelper(this, dbController);
        List<DBSongInfo> info = helper.getMainSheetSongInfo(MainSheetHelper.SHEET_ALL);
        ArrayList<DBSongInfo> result = DBSongInfo.descSortByPlayTimes(info);

        int count = getResources().getInteger(R.integer.rmp_count);
        count = count > result.size() ? result.size() : count;
        // 左闭右开
        for (DBSongInfo in : result.subList(0, count)) {
            RMPAdapter.DataHolder da = new RMPAdapter.DataHolder();
            da.times = in.playTimes;

            SongInfo songInfo = mediaManager.getSongInfo(this, in.path);
            String title = songInfo.getTitle();
            String arts = songInfo.getArtist();
            String albumP = songInfo.getAlbum_path();

            da.album = albumP;
            da.title = title;
            da.arts = arts;
            da.path = in.path;

            data.add(da);
        }

    }

    private void initList() {
        list.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (isActivityFirstCreate) {
            initData();
            isActivityFirstCreate = false;
        }
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

        list = (RecyclerView) findViewById(R.id.rmp_a_list);

        adapter = new RMPAdapter(this, data);
        list.setLayoutManager(new LinearLayoutManager(this));

        line = findViewById(R.id.rmp_a_line);
        title = (TextView) findViewById(R.id.rmp_a_title);
        toolbar = (Toolbar) findViewById(R.id.rmp_a_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        first = new FirstThreeViewHolder(this, FirstThreeViewHolder.FIRST);
        second = new FirstThreeViewHolder(this, FirstThreeViewHolder.SECOND);
        third = new FirstThreeViewHolder(this, FirstThreeViewHolder.THIRD);
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

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        ThemeEnum th = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, th);

        int statusC = cs[0];
        int toolbarC = cs[1];
        int accentC = cs[2];
        int mainBC = cs[3];
        int vicBC = cs[4];
        int mainTC = cs[5];
        int vicTC = cs[6];
        int navC = cs[7];
        int toolbarMainTC = cs[8];
        int toolbarVicTC = cs[9];

        adapter.themeChange(th, cs);

        optionsDialog.setTitleBarBgColor(vicBC);
        optionsDialog.setContentBgColor(mainBC);
        optionsDialog.setDivideColor(vicTC);
        optionsDialog.setTitleTextColor(mainTC);

        optionsAdapter.setTitleColor(mainTC);
        optionsAdapter.setIconColor(accentC);

        FirstThreeViewHolder[] holders = {first, second, third};
        for (FirstThreeViewHolder holder : holders) {
            holder.number.setTextColor(mainBC);
            holder.name.setTextColor(mainTC);
            holder.arts.setTextColor(vicTC);

            holder.time.setTextColor(toolbarMainTC);
            holder.time.setBackgroundColor(accentC);
        }

        line.setBackgroundColor(accentC);
        title.setTextColor(mainTC);

        CollapsingToolbarLayout coll = (CollapsingToolbarLayout) findViewById(R.id.rmp_a_coll_toolbar);
        int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        coll.setStatusBarScrimColor(ta[0]);
        coll.setContentScrimColor(ta[1]);

        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }

    }

    @Override
    public void onItemClick(RMPAdapter.DataHolder data, RMPAdapter.ViewHolder view, int position) {

        RMPAdapter.DataHolder dataHolder = this.data.get(position);
        Song song = new Song(dataHolder.path);

        currentClickItem = mediaManager.getSongInfo(this, song);

        DBSongInfo info = dbController.getSongInfo(song);
        if (info != null) {
            currentClickItemFavorite = info.favorite;
        }

        showDialogIfNeed();

    }

    private void showDialogIfNeed() {

        if (optionsDialog.visible()) {
            optionsDialog.hide();
        } else {
            if (currentClickItem != null) {
                String title = getString(R.string.song) + ": " + currentClickItem.getTitle();
                optionsDialog.setTitle(title);

                String favorite = getString(R.string.favorite);
                if (currentClickItemFavorite) {
                    favorite = getString(R.string.cancel_favorite);
                }
                OptionsAdapter.Option option = optionsAdapter.getOption(SONG_OPTIONS_FAVORITE);
                if (option != null) {
                    option.title = favorite;
                }

                optionsDialog.show();
            }
        }
    }

    // 前三首歌曲点击事件
    public void firstThreeClick(View view) {
        String path = (String) view.getTag(R.id.rmp_first_three_song_path);

        Song song = new Song(path);
        currentClickItem = mediaManager.getSongInfo(this, song);

        DBSongInfo info = dbController.getSongInfo(song);
        if (info != null) {
            currentClickItemFavorite = info.favorite;
        }

        showDialogIfNeed();
    }

    private static class FirstThreeViewHolder {
        Activity activity;

        TextView number;
        ImageView image;
        TextView name;
        TextView arts;
        TextView time;

        final static String FIRST = "first";
        final static String SECOND = "second";
        final static String THIRD = "third";

        FirstThreeViewHolder(Activity activity, String which) {
            this.activity = activity;
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

                number = (TextView) activity.findViewById(idClass.getField(nu).getInt(R.id.class));
                image = (ImageView) activity.findViewById(idClass.getField(im).getInt(R.id.class));
                name = (TextView) activity.findViewById(idClass.getField(na).getInt(R.id.class));
                arts = (TextView) activity.findViewById(idClass.getField(ar).getInt(R.id.class));
                time = (TextView) activity.findViewById(idClass.getField(ti).getInt(R.id.class));

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

}
