package com.duan.musicoco.play;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
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
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.ExceptionHandler;
import com.duan.musicoco.app.OnServiceConnect;
import com.duan.musicoco.app.OnThemeChange;
import com.duan.musicoco.app.PlayServiceManager;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.play.album.VisualizerFragment;
import com.duan.musicoco.play.album.VisualizerPresenter;
import com.duan.musicoco.play.lyric.LyricFragment;
import com.duan.musicoco.play.lyric.LyricPresenter;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.service.PlayServiceCallback;
import com.duan.musicoco.util.AnimationUtils;
import com.duan.musicoco.util.PeriodicTask;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.RealtimeBlurView;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import java.util.List;

import static com.duan.musicoco.preference.Theme.DARK;
import static com.duan.musicoco.preference.Theme.WHITE;

/**
 * Created by DuanJiaNing on 2017/5/23.
 */

public class PlayActivity extends RootActivity implements
        PlayServiceCallback,
        OnServiceConnect,
        View.OnClickListener,
        View.OnLongClickListener,
        IPlayActivity,
        OnThemeChange {

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
    private boolean isListShowing = false;
    private boolean isListBarHide = false;
    private boolean changeColorFollowAlbum = true;

    private PlayServiceConnection mServiceConnection;
    private PeriodicTask periodicTask;
    private PlayListAdapter playListAdapter;
    protected final PlayPreference playPreference;

    public PlayActivity() {
        playPreference = new PlayPreference(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

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
    protected void onPause() {
        super.onPause();
        savePreference();
    }

    private void savePreference() {

        try {
            IPlayControl control = mServiceConnection.takeControl();
            Song song = control.currentSong();
            String path = song.path;
            int index = control.currentSongIndex();
            int pro = sbSongProgress.getProgress();
            int mode = control.getPlayMode();

            playPreference.updateSong(new PlayPreference.CurrentSong(path, pro, index));
            playPreference.updatePlayMode(mode);

        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }
    }

    @Override
    public void onBackPressed() {
        if (isListShowing) {
            hidePlayList();
            return;
        }

        super.onBackPressed();
    }


    @Override
    public void songChanged(Song song, int index) {

        //FIXME
        //更换专辑图片，计算出颜色值
        visualizerPresenter.songChanged(song, 1, changeColorFollowAlbum);

        synchronize(song);
    }

    /**
     * 同步当前播放歌曲
     */
    public void synchronize(@Nullable Song song) {

        if (song == null) {
            try {
                song = mServiceConnection.takeControl().currentSong();
            } catch (RemoteException e) {
                e.printStackTrace();
                new ExceptionHandler().handleRemoteException(this,
                        this.getString(R.string.exception_remote), null
                );
                return;
            }
        }

        if (song == null)
            return;

        int pro = 0;
        try {
            pro = mServiceConnection.takeControl().getProgress();
        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }

        SongInfo info = mediaManager.getSongInfo(song);

        updateData((int) info.getDuration(), pro, info.getTitle(), info.getArtist());

        if (changeColorFollowAlbum) {
            updateColors(visualizerFragment.getCurrColors());
        }

    }

    @Override
    public void startPlay(Song song, int index, int status) {
        periodicTask.start();

        visualizerPresenter.startPlay();
        btPlay.setPlayStatus(true);
    }

    @Override
    public void stopPlay(Song song, int index, int status) {
        periodicTask.stop();

        visualizerPresenter.stopPlay();
        btPlay.setPlayStatus(false);
    }


    /**
     * 更新颜色（当主题为 随专辑变化 时）<br>
     * 1 背景颜色<br>
     * 2 歌曲名字，艺术家字体颜色<br>
     * 3 进度条颜色，进度条下文字颜色<br>
     * 4 上、下曲，暂停按钮颜色<br>
     * 5 播放列表头颜色，背景色<br>
     * 6 调用方法更新播放列表颜色模式（亮，暗）<br>
     * <p>
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
        btPlay.setSolidColor(colors[1]);
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
        Theme theme;
        if (d - 0.400 > 0.000001)
            theme = WHITE;
        else
            theme = DARK;

        playListAdapter.themeChange(theme);

    }

    /**
     * 更新数值和文字<br>
     * 1 进度条当前进度和最大值<br>
     * 2 进度条左右文字（进度，总时长）<br>
     * 3 歌曲名，艺术家<br>
     */
    private void updateData(int duration, int progress, String title, String arts) {

        tvDuration.setText(Utils.getGenTime(duration));
        tvPlayProgress.setText(Utils.getGenTime(progress));

        sbSongProgress.setMax(duration);
        sbSongProgress.setProgress(progress);

        tsSongName.setText(title);
        tsSongArts.setText(arts);
    }

    private void updatePlayMode() {
        Drawable drawable = null;
        StringBuilder builder = new StringBuilder();
        int num = 0;
        int mode = PlayController.MODE_LIST_LOOP;

        try {
            mode = mServiceConnection.takeControl().getPlayMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        switch (mode) {
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
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }

        if (num != 0)
            builder.append(' ').append('(').append(num).append(')');

        tvPlayMode.setText(builder.toString());

    }

    @Override
    public void permissionGranted(int requestCode) {
        super.permissionGranted(requestCode);

        mServiceConnection = new PlayServiceConnection(this, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);
    }

    @Override
    public void permissionDenied(int requestCode) {
        finish();
    }

    @Override
    protected void initViews() {

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

        Theme theme = playPreference.getTheme();
        int mainTextColor = Color.DKGRAY;
        int vicTextColor = Color.GRAY;
        if (theme == Theme.DARK) {
            mainTextColor = getResources().getColor(R.color.theme_dark_main_text);
            vicTextColor = getResources().getColor(R.color.theme_dark_vic_text);
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
        themeChange(theme);

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
                periodicTask.stop();

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                if (change) {
                    try {
                        mServiceConnection.takeControl().seekTo(pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        new ExceptionHandler().handleRemoteException(getApplicationContext(),
                                getApplicationContext().getString(R.string.exception_remote), null
                        );
                    }
                }
                periodicTask.start();
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

        //更新列表数据（当前播放歌曲）
        playListAdapter.notifyDataSetChanged();

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

        if (playPreference.getTheme() == WHITE) {
            startAlphaAnim(duration, null, 0.0f, 0.6f);
        } else {
            vDarkBg.setBackgroundColor(Color.TRANSPARENT);
        }

    }

    @Override
    public void hidePlayList() {
        isListShowing = false;
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

        if (playPreference.getTheme() == WHITE) {
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

    /**
     * 更新播放列表在显示和只显示头部时的显示内容等
     */
    private void updatePlayListBar(boolean show) {
        if (show) {
            vgListHideBar.setVisibility(View.GONE);
            vgListShowBar.setVisibility(View.VISIBLE);
        } else {
            vgListHideBar.setVisibility(View.VISIBLE);
            vgListShowBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showDetail() {
        //TODO
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre_song:
                try {
                    mServiceConnection.takeControl().pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
                break;
            case R.id.play_next_song:
                try {
                    mServiceConnection.takeControl().next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
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
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
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
                showDetail();
                break;
            case R.id.play_mode:
                updatePlayMode();
                try {
                    int mode = mServiceConnection.takeControl().getPlayMode();
                    mode = ((mode - 21) + 1) % 3 + 21;
                    mServiceConnection.takeControl().setPlayMode(mode);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
                break;
            case R.id.play_location:
                try {
                    int index = mServiceConnection.takeControl().currentSongIndex();
                    lvPlayList.smoothScrollToPosition(index);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    new ExceptionHandler().handleRemoteException(this,
                            this.getString(R.string.exception_remote), null
                    );
                }
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


    @Override
    public void onConnected(ComponentName name, IBinder service) {

        initSelfData();
        visualizerPresenter.initData(null);
        lyricPresenter.initData(null);

    }

    private void initSelfData() {

        visualizerPresenter = new VisualizerPresenter(this, mServiceConnection.takeControl(), visualizerFragment);
        lyricPresenter = new LyricPresenter(this, lyricFragment, this);

        periodicTask = new PeriodicTask(new PeriodicTask.Task() {
            int progress;

            @Override
            public void execute() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress = mServiceConnection.takeControl().getProgress();
                            sbSongProgress.setProgress(progress);
                            tvPlayProgress.setText(Utils.getGenTime(progress));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            new ExceptionHandler().handleRemoteException(getApplicationContext(),
                                    getApplicationContext().getString(R.string.exception_remote), null
                            );
                        }
                    }
                });
            }
        }, 800);

        //更新播放模式
        updatePlayMode();

        //预先设置播放列表外观
        if (playListAdapter == null)
            playListAdapter = new PlayListAdapter(this, mServiceConnection.takeControl());
        lvPlayList.setAdapter(playListAdapter);

        //更新播放列表字体颜色模式（亮 暗）
        Theme theme = playPreference.getTheme();
        int alpha = getResources().getInteger(R.integer.play_list_bg_alpha);
        int color;
        switch (theme) {
            case DARK: {
                color = getResources().getColor(R.color.theme_dark_vic_text);
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

        //检查播放列表是否为空
        List<Song> songs = null;
        try {
            songs = mServiceConnection.takeControl().getPlayList();
        } catch (RemoteException e) {
            e.printStackTrace();
            new ExceptionHandler().handleRemoteException(this,
                    this.getString(R.string.exception_remote), null
            );
        }
        if (songs == null) {
            noSongsInDisk();
            updateData(0, 0, "", "");
        } else { //服务端在 onCreate 时会回调 songChanged ，PlayActivity 第一次绑定可能接收不到此次回调
            try {

                Song song = mServiceConnection.takeControl().currentSong();
                int index = songs.indexOf(song);
                songChanged(song, index);

            } catch (RemoteException e) {
                e.printStackTrace();
                new ExceptionHandler().handleRemoteException(this,
                        this.getString(R.string.exception_remote), null
                );
            }
        }
    }

    @Override
    public void disConnected(ComponentName name) {
        mServiceConnection = null;
        mServiceConnection = new PlayServiceConnection(this, this, this);
        PlayServiceManager.bindService(this, mServiceConnection);
    }

    @Override
    public void themeChange(Theme theme) {

        int colors[] = new int[4];

        switch (theme) {
            case WHITE:
                changeColorFollowAlbum = false;
                colors = com.duan.musicoco.util.ColorUtils.getThemeWhiteColors(this);
                break;
            case VARYING:
                changeColorFollowAlbum = true;
                break;
            case DARK:
            default:
                changeColorFollowAlbum = false;
                colors = com.duan.musicoco.util.ColorUtils.getThemeDarkColors(this);
                break;
        }
        updateColors(colors);
    }
}
