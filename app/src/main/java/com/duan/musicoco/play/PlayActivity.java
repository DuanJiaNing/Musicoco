package com.duan.musicoco.play;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.duan.musicoco.BasePresenter;
import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.Init;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.PermissionManager;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.fragment.album.VisualizerFragment;
import com.duan.musicoco.fragment.album.VisualizerPresenter;
import com.duan.musicoco.fragment.lyric.LyricFragment;
import com.duan.musicoco.fragment.lyric.LyricPresenter;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements ActivityViewContract, View.OnClickListener, View.OnLongClickListener {

    private final PlayServiceConnection mServiceConnection;
    private MediaManager mediaManager;

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;

    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private TextView tvPlayProgress;
    private TextView tvDuration;
    private TextSwitcher tsSongName;
    private TextSwitcher tsSongArts;
    private DiscreteSeekBar sbSongProgress;

    private TextView tvPlayMode;
    private int currentPlayMode;

    private FrameLayout flFragmentContainer;
    private FrameLayout flRootView;
    private FrameLayout flList;
    private ConstraintLayout clName;

    private PlayView btPlay;
    private SkipView btPre;
    private SkipView btNext;
    private ImageButton btMore;
    private ImageButton btLocation;

    private ListView lvPlayList;

    private FragmentManager fragmentManager;

    private boolean isFragmentAniming = false;
    private boolean isListShowing = false;

    private PlayPreference playPreference;

    private Song currentSong;

    private View vDarkBg;

    int currentIndex;
    //只有手动切换上一曲时才需要让 AlbumPicture 执行'上一曲'的切换动画
    boolean isPre = false;

    private PlayListAdapter playListadapter;

    private boolean changeColorFollowAlbum = true;

    public PlayActivity() {
        mServiceConnection = new PlayServiceConnection(this, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        playPreference = new PlayPreference(getApplicationContext());

        initViews(null, null);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionManager.PerMap.CATEGORY_MEDIA_READ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                permissionGranted();
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mServiceConnection.hasConnected) {
            mServiceConnection.unregisterListener();
            unbindService(mServiceConnection);
        }
    }

    @Override
    public void permissionGranted() {
        PlayServiceManager.bindService(this, mServiceConnection);
        mediaManager = MediaManager.getInstance(getApplicationContext());

        new Thread() {
            @Override
            public void run() {
                mediaManager.refreshData();
                new Init().initImageCache(PlayActivity.this);
            }
        }.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentSong != null) {
            String path = currentSong.path;
            int index = currentIndex;
            int pro = sbSongProgress.getProgress();
            playPreference.updateCurrentSong(new PlayPreference.CurrentSong(path, pro, index));
            playPreference.updateCurrentPlayMode(currentPlayMode);
        }
    }

    @Override
    public void songChanged(Song song, int index) {

        currentSong = song;
        currentIndex = index;

        int switchTo = isPre ? 0 : 1;
        isPre = false;

        //更换专辑图片，计算出颜色值
        visualizerPresenter.songChanged(song, switchTo, changeColorFollowAlbum);

        if (changeColorFollowAlbum)
            //FIXME 既使用 visualizerPresenter 控制 fragment，又通过 visualizerFragment 直接控制（那干嘛要用 MVP）
            updateColors(visualizerFragment.getCurrColors());

        SongInfo info = mediaManager.getSongInfo(song);
        updateData((int) info.getDuration(), 0, info.getTitle(), info.getArtist());

        if (playListadapter != null)
            playListadapter.notifyDataSetChanged();

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        startUpdateProgressTask();

        visualizerPresenter.startPlay();
        btPlay.setPlayStatus(true);
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        cancelUpdateProgressTask();

        visualizerPresenter.stopPlay();
        btPlay.setPlayStatus(false);
    }

    /**
     * 0 暗的活力颜色 主背景色<br>
     * 1 暗的活力颜色 对应适合的字体颜色 主字体色<br>
     * 2 暗的柔和颜色 辅背景色<br>
     * 3 暗的柔和颜色 对应适合的字体颜色 辅字体色<br>
     */
    private void updateColors(int[] colors) {
        if (colors.length != 4)
            return;

        ((TextView) (tsSongName.getCurrentView())).setTextColor(colors[1]);
        ((TextView) (tsSongArts.getCurrentView())).setTextColor(colors[3]);

        ColorDrawable cd = (ColorDrawable) flRootView.getBackground();
        if (cd != null) {
            if (cd.getColor() != colors[0]) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AnimationUtils.startColorGradientAnim(1000, flRootView, cd.getColor(), colors[0]);
                } else flRootView.setBackgroundColor(colors[0]);
            }
        } else flRootView.setBackgroundColor(colors[0]);

        tvPlayProgress.setTextColor(colors[3]);
        tvDuration.setTextColor(colors[3]);

        btPre.setTriangleColor(colors[1]);
        btNext.setTriangleColor(colors[1]);

        btPlay.setTriangleColor(colors[1]);
        btPlay.setStrokeColor(colors[1]);
        btPlay.setPauseLineColor(colors[1]);

        sbSongProgress.setRippleColor(colors[3]);
        sbSongProgress.setScrubberColor(colors[1]);
        sbSongProgress.setThumbColor(colors[3], colors[1]);
        sbSongProgress.setTrackColor(colors[3]);

        flFragmentContainer.setBackgroundColor(Color.TRANSPARENT);

    }

    private void updateData(int duration, int progress, String title, String arts) {

        tvDuration.setText(Utils.getGenTime(duration));
        tvPlayProgress.setText(Utils.getGenTime(progress));
        sbSongProgress.setProgress(progress);
        sbSongProgress.setMax(duration);
        tsSongName.setText(title);
        tsSongArts.setText(arts);
    }

    private void updatePlayMode() {
        Drawable drawable = null;
        StringBuilder builder = new StringBuilder();
        int num = 0;
        switch (currentPlayMode) {
            case PlayController.MODE_LIST_LOOP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getDrawable(R.drawable.list_loop);
                } else drawable = getResources().getDrawable(R.drawable.list_loop);
                builder.append(getString(R.string.play_mode_list_loop));
                break;

            case PlayController.MODE_SINGLE_LOOP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getDrawable(R.drawable.single_loop);
                } else drawable = getResources().getDrawable(R.drawable.single_loop);
                builder.append(getString(R.string.play_mode_single_loop));
                break;

            case PlayController.MODE_RANDOM:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getDrawable(R.drawable.random);
                } else drawable = getResources().getDrawable(R.drawable.random);
                builder.append(getString(R.string.play_mode_random));
                break;
        }

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tvPlayMode.setCompoundDrawables(drawable, null, null, null);

        try {
            num = mServiceConnection.takeControl().getPlayList().size();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (num != 0)
            builder.append(' ').append('(').append(num).append(')');

        tvPlayMode.setText(builder.toString());

    }

    //设置主题风格
    public void setThemeMode(Theme themeMode) {

        int colors[] = new int[4];

        switch (themeMode) {
            case WHITE:
                changeColorFollowAlbum = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colors[0] = getColor(R.color.theme_white_main_bg); //主背景色
                    colors[1] = getColor(R.color.theme_white_main_text); // 主字体色
                    colors[2] = getColor(R.color.theme_white_vic_bg); // 辅背景色
                    colors[3] = getColor(R.color.theme_white_vic_text); // 辅字体色
                } else {
                    colors[0] = getResources().getColor(R.color.theme_white_main_bg); //主背景色
                    colors[1] = getResources().getColor(R.color.theme_white_main_text); // 主字体色
                    colors[2] = getResources().getColor(R.color.theme_white_vic_bg); // 辅背景色
                    colors[3] = getResources().getColor(R.color.theme_white_vic_text); // 辅字体色
                }
                break;
            case VARYING:
                changeColorFollowAlbum = true;
                break;
            case DARKGOLD:
            default:
                changeColorFollowAlbum = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    colors[0] = getColor(R.color.theme_dark_gold_main_bg); //主背景色
                    colors[1] = getColor(R.color.theme_dark_gold_main_text); // 主字体色
                    colors[2] = getColor(R.color.theme_dark_gold_vic_bg); // 辅背景色
                    colors[3] = getColor(R.color.theme_dark_gold_vic_text); // 辅字体色
                } else {
                    colors[0] = getResources().getColor(R.color.theme_dark_gold_main_bg); //主背景色
                    colors[1] = getResources().getColor(R.color.theme_dark_gold_main_text); // 主字体色
                    colors[2] = getResources().getColor(R.color.theme_dark_gold_vic_bg); // 辅背景色
                    colors[3] = getResources().getColor(R.color.theme_dark_gold_vic_text); // 辅字体色
                }
                break;
        }
        updateColors(colors);
    }

    private TimerTask progressUpdateTask;

    public void cancelUpdateProgressTask() {
        if (progressUpdateTask != null)
            progressUpdateTask.cancel();
    }

    public void startUpdateProgressTask() {
        Timer timer = new Timer();
        progressUpdateTask = new TimerTask() {
            int progress;

            @Override
            public void run() {
                PlayActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress = mServiceConnection.takeControl().getProgress();
                            sbSongProgress.setProgress(progress);
                            tvPlayProgress.setText(Utils.getGenTime(progress));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(progressUpdateTask, 0, 800);
    }

    //服务成功连接之后才初始化数据
    @Override
    public void onConnected() {

        initSelfData();

        visualizerPresenter.initData(null);
        lyricPresenter.initData(null);

    }

    @Override
    public void disConnected() {

    }

    private void initSelfData() {

        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

        Song song = null;

        currentPlayMode = playPreference.getCurrentPlayMode();
        updatePlayMode();

        PlayPreference.CurrentSong cur = playPreference.getCurrentSong();
        if (cur != null && cur.path != null)
            song = new Song(cur.path);

        List<Song> songs = null;

        try {
            songs = mServiceConnection.takeControl().getPlayList();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (songs == null) {
            noSongsInDisk();
            updateData(0, 0, "", "");
            return;
        }

        if (song == null) { //配置文件没有保存【最后播放曲目】信息（通常为第一次打开应用）
            song = songs.get(0);
        } else { //配置文件有保存
            if (!songs.contains(song)) { //确认服务端有此歌曲
                song = songs.get(0);
            }
        }

        try {

            mServiceConnection.takeControl().setCurrentSong(song);

            int pro = cur.progress;
            if (pro >= 0) {
                mServiceConnection.takeControl().seekTo(pro);
                sbSongProgress.setProgress(pro);
            }

            boolean st = mServiceConnection.takeControl().status() == PlayController.STATUS_PLAYING;
            btPlay.setPlayStatus(st);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (playListadapter == null)
            playListadapter = new PlayListAdapter(this, mServiceConnection.takeControl());
        lvPlayList.setAdapter(playListadapter);

    }

    @Override
    public void initViews(@Nullable View view, Object obj) {

        //初始控件
        flRootView = (FrameLayout) findViewById(R.id.play_root);
        tvPlayProgress = (TextView) findViewById(R.id.play_progress);
        tvDuration = (TextView) findViewById(R.id.play_duration);
        sbSongProgress = (DiscreteSeekBar) findViewById(R.id.play_seekBar);
        tsSongName = (TextSwitcher) findViewById(R.id.play_ts_song_name);
        tsSongArts = (TextSwitcher) findViewById(R.id.play_ts_song_arts);
        btPre = (SkipView) findViewById(R.id.play_pre_song);
        btNext = (SkipView) findViewById(R.id.play_next_song);
        btPlay = (PlayView) findViewById(R.id.play_song);
        btMore = (ImageButton) findViewById(R.id.play_more);
        btLocation = (ImageButton) findViewById(R.id.play_location);
        flFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);
        flList = (FrameLayout) findViewById(R.id.play_list);
        clName = (ConstraintLayout) findViewById(R.id.play_name);
        tvPlayMode = (TextView) findViewById(R.id.play_mode);
        lvPlayList = (ListView) findViewById(R.id.play_play_list);
        vDarkBg = findViewById(R.id.play_dark_bg);


        Theme theme = new AppPreference(this).getTheme();
        theme = Theme.VARYING;
        int mainTextColor = Color.DKGRAY;
        int vicTextColor = Color.GRAY;
        if (theme == Theme.DARKGOLD) {
            mainTextColor = getResources().getColor(R.color.theme_dark_gold_main_text);
            vicTextColor = getResources().getColor(R.color.theme_dark_gold_vic_text);
        } else if (theme == Theme.WHITE) {
            mainTextColor = getResources().getColor(R.color.theme_white_main_text);
            vicTextColor = getResources().getColor(R.color.theme_white_vic_text);
        }
        //设置属性
        final int finalMainTextColor = mainTextColor;
        tsSongName.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_name, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/name.TTF");
                text.setTypeface(tf);
                text.setTextColor(finalMainTextColor);
                return text;
            }
        });
        final int finalVicTextColor = vicTextColor;
        tsSongArts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) getLayoutInflater().inflate(R.layout.play_arts, null);
                Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/arts.TTF");
                text.setTypeface(tf);
                text.setTextColor(finalVicTextColor);
                return text;
            }
        });

        //设置主题
        setThemeMode(theme);

        vDarkBg.setOnClickListener(this);
        btLocation.setOnClickListener(this);
        tvPlayMode.setOnClickListener(this);
        clName.setOnClickListener(this);
        flFragmentContainer.setOnClickListener(this);
        flFragmentContainer.setOnLongClickListener(this);
        btPre.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPlay.setOnClickListener(this);
        btMore.setOnClickListener(this);

        tvPlayMode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        btMore.setEnabled(false);
        btMore.setVisibility(View.INVISIBLE);

        sbSongProgress.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return value / 1000;
            }
        });
        sbSongProgress.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            int pos;
            boolean change = false;

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {

                pos = value;
                tvPlayProgress.setText(Utils.getGenTime(value));
                change = true;

            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                cancelUpdateProgressTask();

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                if (change) {
                    try {
                        mServiceConnection.takeControl().seekTo(pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                startUpdateProgressTask();
            }
        });

        ViewGroup.LayoutParams params = flList.getLayoutParams();
        DisplayMetrics metrics = Utils.getMetrics(this);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = metrics.heightPixels / 2;
        flList.setX(0);
        flList.setY(metrics.heightPixels);
        flList.setLayoutParams(params);

        lyricFragment = new LyricFragment();
        visualizerFragment = new VisualizerFragment();

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //顺序 依次叠放
        transaction.add(R.id.play_fragment_container, visualizerFragment, VisualizerFragment.TAG);
        //TODO lyricFragment
        transaction.add(R.id.play_fragment_container, lyricFragment, LyricFragment.TAG);
        transaction.hide(lyricFragment);
        transaction.commit();

    }

    @Override
    public void setPresenter(BasePresenter presenter) {
    }

    @Override
    public void onBackPressed() {
        if (isListShowing) {
            hidePlayList();
            return;
        }

        if (fragmentManager.findFragmentByTag(LyricFragment.TAG).isVisible())
            hideLyricFragment();
        else
            super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre_song:
                isPre = true;
                try {
                    mServiceConnection.takeControl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_next_song:
                try {
                    mServiceConnection.takeControl().next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_song:

                try {
                    int stat = mServiceConnection.takeControl().status();
                    if (stat == PlayController.STATUS_PLAYING) {
                        mServiceConnection.takeControl().pause();
                    } else {
                        mServiceConnection.takeControl().resume();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_more:
                Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
                break;
            case R.id.play_fragment_container:
                if (!isListShowing)
                    showPlayList();
                break;
            case R.id.play_name:
                break;
            case R.id.play_mode:
                currentPlayMode = ((currentPlayMode - 21) + 1) % 3 + 21;
                updatePlayMode();
                try {
                    mServiceConnection.takeControl().setPlayMode(currentPlayMode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_location:
                lvPlayList.smoothScrollToPosition(currentIndex);
                break;
            case R.id.play_dark_bg:
                if (isListShowing)
                    hidePlayList();
                break;
            default:
                break;
        }
    }

    //最上面的一定是 VisualizerFragment
    @Override
    public void showLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(lyricFragment);
        transaction.commit();
    }

    //最上面的一定是 LyricFragment
    @Override
    public void hideLyricFragment() {

        if (isFragmentAniming)
            return;

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.show(visualizerFragment); // 显示但被 LyricFragment 遮挡
        transaction.commit();

    }

    @Override
    public void noSongsInDisk() {
        btPlay.setEnabled(false);
        btPre.setEnabled(false);
        btNext.setEnabled(false);
        sbSongProgress.setEnabled(false);
    }

    private void startTranslateYAnim(float from, float to, int duration, final View view, @Nullable TimeInterpolator interpolator, @Nullable Animator.AnimatorListener listener) {
        final ValueAnimator anim = ObjectAnimator.ofFloat(from, to);
        anim.setDuration(duration);
        if (interpolator != null)
            anim.setInterpolator(interpolator);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                view.setY(va);
                ViewGroup.LayoutParams params = vDarkBg.getLayoutParams();
                params.height = (int) va;
                vDarkBg.setLayoutParams(params);
//                vDarkBg.setBottom((int) va);
            }
        });
        if (listener != null)
            anim.addListener(listener);
        anim.start();
    }

    private void startAlphaAnim(int duration, @Nullable Animator.AnimatorListener listener, float... values) {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(values);
        alphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (float) animation.getAnimatedValue();
                vDarkBg.setAlpha(alpha);
            }
        });
        if (listener != null)
            alphaAnim.addListener(listener);

        alphaAnim.setDuration(duration);
        alphaAnim.start();
    }

    @Override
    public void showPlayList() {
        isListShowing = true;
        int duration = getResources().getInteger(R.integer.play_list_anim_duration);

        DisplayMetrics metrics = Utils.getMetrics(this);
        startTranslateYAnim(
                metrics.heightPixels,
                metrics.heightPixels / 2,
                duration,
                flList,
                new AccelerateInterpolator(), null
        );

        vDarkBg.setVisibility(View.VISIBLE);
        vDarkBg.setClickable(true);
        startAlphaAnim(duration, null, 0.0f, 0.7f);

    }

    @Override
    public void hidePlayList() {
        isListShowing = false;
        int duration = getResources().getInteger(R.integer.play_list_anim_duration);

        DisplayMetrics metrics = Utils.getMetrics(this);
        startTranslateYAnim(
                metrics.heightPixels / 2,
                metrics.heightPixels,
                duration,
                flList,
                new DecelerateInterpolator(), null
        );

        vDarkBg.setClickable(false);
        startAlphaAnim(duration, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                vDarkBg.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }, 0.7f, 0.0f);

    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.play_fragment_container:
                showDetailDialog();
                return true;
        }
        return false;
    }

    private void showDetailDialog() {
        int startColor = ((ColorDrawable) (flRootView.getBackground())).getColor();
        int endColor = btPlay.getTriangleColor();
        ValueAnimator anim = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ObjectAnimator.ofArgb(startColor, endColor);
        }
        if (anim == null)
            return;

        anim.setTarget(flFragmentContainer);
        anim.setDuration(300);
        anim.setRepeatCount(1);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Toast.makeText(PlayActivity.this, "time to show dialog", Toast.LENGTH_SHORT).show();
            }
        });
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int co = (int) animation.getAnimatedValue();
                flFragmentContainer.setBackgroundColor(co);
            }
        });

        anim.start();

    }
}
