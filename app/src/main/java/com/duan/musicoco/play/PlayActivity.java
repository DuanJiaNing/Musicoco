package com.duan.musicoco.play;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.album.VisualizerPresenter;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.play.lyric.LyricPresenter;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealtimeBlurView;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.duan.musicoco.preference.Theme.WHITE;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements View.OnClickListener, View.OnLongClickListener, IPlayActivity {

    private VisualizerFragment visualizerFragment;
    private LyricFragment lyricFragment;
    private VisualizerPresenter visualizerPresenter;
    private LyricPresenter lyricPresenter;

    private TextView tvPlayProgress;
    private TextView tvDuration;
    private TextSwitcher tsSongName;
    private TextSwitcher tsSongArts;
    private DiscreteSeekBar sbSongProgress;
    private FrameLayout flFragmentContainer;
    private FrameLayout flRootView;
    private CardView cvList;
    private ConstraintLayout clName;
    private PlayView btPlay;
    private SkipView btPre;
    private SkipView btNext;
    private ImageButton btMore;
    private ListView lvPlayList;
    private RealtimeBlurView rtbPlayList;
    private View rlListBarContainer;
    private View vDarkBg;
    private LinearLayout llRootMain;

    private ViewGroup vgListShowBar;
    private ImageButton btLocation;

    private ViewGroup vgListHideBar;
    private ImageButton btHideListBar;
    private TextView tvPlayMode;

    private FragmentManager fragmentManager;
    private int currentPlayMode;
    private boolean isListShowing = false;
    private boolean isListBarHide = false;
    private boolean changeColorFollowAlbum = true;

    private Song currentSong;

    private PlayListAdapter playListAdapter;

    int currentIndex;
    //只有手动切换上一曲时才需要让 AlbumPictureController 执行'上一曲'的切换动画
    boolean isPre = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initViews();

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
    public void permissionDenied(int requestCode) {
        finish();
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

        SongInfo info = mediaManager.getSongInfo(song);
        updateData((int) info.getDuration(), 0, info.getTitle(), info.getArtist());

        if (changeColorFollowAlbum) {
            //FIXME 既使用 visualizerPresenter 控制 fragment，又通过 visualizerFragment 直接控制（那干嘛要用 MVP）
            updateColors(visualizerFragment.getCurrColors());

            if (playListAdapter != null)
                playListAdapter.notifyDataSetChanged();
        }
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

        int alpha = getResources().getInteger(R.integer.play_list_bg_alpha);
        int color = ColorUtils.setAlphaComponent(colors[2], alpha);
        rtbPlayList.setOverlayColor(color);
        rlListBarContainer.setBackgroundColor(colors[2]);

        updatePlayListColorMode();

    }

    // 0 字体颜色为暗色
    // 1 字体颜色为亮色
    private void updatePlayListColorMode() {

        if (playListAdapter == null)
            return;

        int color = rtbPlayList.getOverlayColor();
        double d = ColorUtils.calculateLuminance(color);
        int mode;
        if (d - 0.400 > 0.000001)
            mode = 0;
        else
            mode = 1;

        playListAdapter.setMode(mode);

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
    private void updateThemeMode(Theme themeMode) {

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

    /**
     * 更新播放列表在显示和只显示头部时的显示内容等
     */
    private void updatePlayListBar(boolean show) {
        //TODO
        if (show) {
        } else {
        }
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

        currentPlayMode = playPreference.getCurrentPlayMode();
        updatePlayMode();


        //预先设置播放列表外观
        if (playListAdapter == null)
            playListAdapter = new PlayListAdapter(this, mServiceConnection.takeControl());
        lvPlayList.setAdapter(playListAdapter);

        Theme theme = appPreference.getTheme();
        int alpha = getResources().getInteger(R.integer.play_list_bg_alpha);
        int color;
        switch (theme) {
            case DARKGOLD: {
                color = getResources().getColor(R.color.theme_dark_gold_vic_text);
                break;
            }
            case VARYING:
            case WHITE:
            default:
                color = getResources().getColor(R.color.theme_white_main_text);
                break;
        }
        color = ColorUtils.setAlphaComponent(color, alpha);
        rtbPlayList.setOverlayColor(color);
        rlListBarContainer.setBackgroundColor(ColorUtils.setAlphaComponent(rtbPlayList.getOverlayColor(), 255));
        updatePlayListColorMode();


        //恢复上次播放状态
        Song song = null;
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
            // songChanged 将被回调
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

    }

    public void initViews() {

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
        flFragmentContainer = (FrameLayout) findViewById(R.id.play_fragment_container);
        cvList = (CardView) findViewById(R.id.play_list);
        clName = (ConstraintLayout) findViewById(R.id.play_name);
        lvPlayList = (ListView) findViewById(R.id.play_play_list);
        vDarkBg = findViewById(R.id.play_dark_bg);
        rtbPlayList = (RealtimeBlurView) findViewById(R.id.play_blur);
        llRootMain = (LinearLayout) findViewById(R.id.play_root_main);

        rlListBarContainer = findViewById(R.id.play_list_bar_container);

        vgListShowBar = (ViewGroup) findViewById(R.id.play_list_show_bar);
        vgListShowBar.setVisibility(View.GONE);
        btLocation = (ImageButton) findViewById(R.id.play_location);

        vgListHideBar = (ViewGroup) findViewById(R.id.play_list_hide_bar);
        vgListHideBar.setVisibility(View.VISIBLE);
        btHideListBar = (ImageButton) findViewById(R.id.play_list_hide);
        tvPlayMode = (TextView) findViewById(R.id.play_mode);

        Theme theme = appPreference.getTheme();
        int mainTextColor = Color.DKGRAY;
        int vicTextColor = Color.GRAY;
        if (theme == Theme.DARKGOLD) {
            mainTextColor = getResources().getColor(R.color.theme_dark_gold_main_text);
            vicTextColor = getResources().getColor(R.color.theme_dark_gold_vic_text);
        } else if (theme == WHITE) {
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
        updateThemeMode(theme);

        vgListHideBar.setOnClickListener(this);
        vgListShowBar.setOnClickListener(this);

        btHideListBar.setOnClickListener(this);
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

        ViewGroup.LayoutParams params = cvList.getLayoutParams();
        DisplayMetrics metrics = Utils.getMetrics(this);
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = metrics.heightPixels / 2;
        cvList.setX(0);
        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        cvList.setY(metrics.heightPixels - marginB);
        cvList.setLayoutParams(params);

    }

    @Override
    public void onBackPressed() {
        if (isListShowing) {
            hidePlayList();
            return;
        }

        if (!isListBarHide) {
            hidePlayListBar();
            return;
        }

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
                showDetailDialog(currentSong);
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
            case R.id.play_list_hide_bar:
            case R.id.play_list_show_bar:
                if (isListShowing)
                    hidePlayList();
                else showPlayList();
                break;
            case R.id.play_list_hide:
                if (!isListBarHide)
                    hidePlayListBar();
                break;
            default:
                break;
        }
    }

    private void hidePlayListBar() {
        isListBarHide = true;

        final int duration = getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        float from = cvList.getY();
        float to = from + marginB;

        startTranslateBarAnim(from, to, duration);
    }

    private void showPlayListBar() {
        isListBarHide = false;

        final int duration = getResources().getInteger(R.integer.play_list_anim_duration);
        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        float from = cvList.getY();
        float to = from - marginB;
        startTranslateBarAnim(from, to, duration);

    }

    private void startTranslateBarAnim(float from, float to, int duration) {
        AnimatorSet set = new AnimatorSet();
        set.setDuration(duration);

        ValueAnimator animY = ObjectAnimator.ofFloat(from, to);

        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        int fromM = isListBarHide ? marginB : 0;
        int toM = isListBarHide ? 0 : marginB;
        ValueAnimator animM = ObjectAnimator.ofInt(fromM, toM);
        animM.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                FrameLayout.LayoutParams param = (FrameLayout.LayoutParams) llRootMain.getLayoutParams();
                int va = (int) animation.getAnimatedValue();
                param.setMargins(0, 0, 0, va);
                llRootMain.setLayoutParams(param);
                llRootMain.requestLayout();

            }
        });

        animY.setDuration(duration);
        animY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float va = (float) animation.getAnimatedValue();
                cvList.setY(va);
            }
        });

        set.play(animY).with(animM);
        set.start();
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
    public void noSongsInDisk() {
        btPlay.setEnabled(false);
        btPre.setEnabled(false);
        btNext.setEnabled(false);
        sbSongProgress.setEnabled(false);
    }

    @Override
    public void showPlayList() {
        isListShowing = true;

        vgListHideBar.setVisibility(View.GONE);
        vgListShowBar.setVisibility(View.VISIBLE);

        final int duration = getResources().getInteger(R.integer.play_list_anim_duration);

        int mb = (int) getResources().getDimension(R.dimen.activity_default_margin);
        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        float from = cvList.getY();
        float to = isListBarHide ? from - (cvList.getHeight() - mb) : from - (cvList.getHeight() - mb - marginB);
        startTranslateYAnim(
                from,
                to,
                duration,
                cvList,
                new AccelerateInterpolator(), null);

        updatePlayListBar(true);

        vDarkBg.setClickable(true);
        vDarkBg.setVisibility(View.VISIBLE);

        if (appPreference.getTheme() == WHITE) {
            startAlphaAnim(duration, null, 0.0f, 0.6f);
        } else {
            vDarkBg.setBackgroundColor(Color.TRANSPARENT);
        }

    }

    @Override
    public void hidePlayList() {
        isListShowing = false;
        vgListHideBar.setVisibility(View.VISIBLE);
        vgListShowBar.setVisibility(View.GONE);

        final int duration = getResources().getInteger(R.integer.play_list_anim_duration);

        int marginB = (int) getResources().getDimension(R.dimen.action_bar_default_height);
        int mb = (int) getResources().getDimension(R.dimen.activity_default_margin);
        float from = cvList.getY();
        float to = isListBarHide ? from + (cvList.getHeight() - mb) : from + (cvList.getHeight() - mb - marginB);
        startTranslateYAnim(
                from,
                to,
                duration,
                cvList,
                new DecelerateInterpolator(), null);

        updatePlayListBar(false);

        vDarkBg.setClickable(false);

        if (appPreference.getTheme() == WHITE) {
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
            }, 0.6f, 0.0f);
        } else
            vDarkBg.setVisibility(View.GONE);


    }

    @Override
    public void showDetailDialog(Song song) {
        //TODO
        Toast.makeText(this, "show detail dialog " + song.path, Toast.LENGTH_SHORT).show();

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

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.play_fragment_container:
                if (isListBarHide)
                    showPlayListBar();
                return true;
            default:
                break;
        }

        return false;
    }
}
