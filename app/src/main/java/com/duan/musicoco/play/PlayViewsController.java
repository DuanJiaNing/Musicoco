package com.duan.musicoco.play;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.preference.PlayPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.service.PlayController;
import com.duan.musicoco.shared.PeriodicTask;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.view.discreteseekbar.DiscreteSeekBar;
import com.duan.musicoco.view.media.PlayView;
import com.duan.musicoco.view.media.SkipView;

import static com.duan.musicoco.preference.ThemeEnum.WHITE;

/**
 * Created by DuanJiaNing on 2017/7/31.
 */

public class PlayViewsController implements View.OnClickListener {

    private Activity activity;

    private TextView tvPlayProgress;
    private TextView tvDuration;
    private TextSwitcher tsSongName;
    private TextSwitcher tsSongArts;
    private DiscreteSeekBar sbSongProgress;
    private PlayView btPlay;
    private SkipView btPre;
    private SkipView btNext;

    private PlayPreference playPreference;
    private PeriodicTask periodicTask;
    private IPlayControl control;

    public PlayViewsController(Activity activity) {
        this.activity = activity;
    }

    public void initViews() {

        tvPlayProgress = (TextView) activity.findViewById(R.id.play_progress);
        tvDuration = (TextView) activity.findViewById(R.id.play_duration);
        sbSongProgress = (DiscreteSeekBar) activity.findViewById(R.id.play_seekBar);

        tsSongName = (TextSwitcher) activity.findViewById(R.id.play_ts_song_name);
        tsSongArts = (TextSwitcher) activity.findViewById(R.id.play_ts_song_arts);

        btPre = (SkipView) activity.findViewById(R.id.play_pre_song);
        btNext = (SkipView) activity.findViewById(R.id.play_next_song);
        btPlay = (PlayView) activity.findViewById(R.id.play_song);

        btPre.setOnClickListener(this);
        btNext.setOnClickListener(this);
        btPlay.setOnClickListener(this);

    }

    public void initData(PlayPreference preference, IPlayControl control) {
        this.playPreference = preference;
        this.control = control;

        // 初始化 歌名，艺术家 TextSwitch
        initSwitchers();
        initPeriodicer();
        initProgress();
    }

    private void initProgress() {

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
                tvPlayProgress.setText(StringUtils.getGenTimeMS(value));
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
                        control.seekTo(pos);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                periodicTask.start();
            }
        });

    }

    private void initPeriodicer() {

        periodicTask = new PeriodicTask(new PeriodicTask.Task() {
            int progress;

            @Override
            public void execute() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            progress = control.getProgress();
                            sbSongProgress.setProgress(progress);
                            tvPlayProgress.setText(StringUtils.getGenTimeMS(progress));

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }, 800);

    }

    private void initSwitchers() {

        ThemeEnum themeEnum = playPreference.getTheme();
        int mainTextColor = Color.DKGRAY;
        int vicTextColor = Color.GRAY;
        if (themeEnum == ThemeEnum.DARK) {
            mainTextColor = activity.getResources().getColor(R.color.theme_dark_main_text);
            vicTextColor = activity.getResources().getColor(R.color.theme_dark_vic_text);
        } else if (themeEnum == WHITE) {
            mainTextColor = activity.getResources().getColor(R.color.theme_white_main_text);
            vicTextColor = activity.getResources().getColor(R.color.theme_white_vic_text);
        }

        //设置属性
        final int finalMainTextColor = mainTextColor;
        tsSongName.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) activity.getLayoutInflater().inflate(R.layout.song_name, null);
                Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/name.TTF");
                text.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                text.setTypeface(tf);
                text.setTextColor(finalMainTextColor);
                text.setSingleLine();
                text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                text.setMarqueeRepeatLimit(-1);
                text.setSelected(true);
                text.setLines(1);
                text.setGravity(Gravity.CENTER);

                return text;
            }
        });
        final int finalVicTextColor = vicTextColor;
        tsSongArts.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView text = (TextView) activity.getLayoutInflater().inflate(R.layout.song_arts, null);
                Typeface tf = Typeface.createFromAsset(activity.getAssets(), "fonts/arts.TTF");
                text.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                text.setTypeface(tf);
                text.setTextColor(finalVicTextColor);
                text.setLines(1);
                text.setGravity(Gravity.CENTER);

                return text;
            }
        });

    }

    public void updateColors(int[] colors) {

        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        ((TextView) (tsSongName.getCurrentView())).setTextColor(mainTC);
        ((TextView) (tsSongName.getNextView())).setTextColor(mainTC);
        ((TextView) (tsSongArts.getCurrentView())).setTextColor(vicTC);
        ((TextView) (tsSongArts.getNextView())).setTextColor(vicTC);

        tvPlayProgress.setTextColor(vicTC);
        tvDuration.setTextColor(vicTC);

        btPre.setTriangleColor(mainTC);
        btNext.setTriangleColor(mainTC);

        btPlay.setTriangleColor(mainTC);
        btPlay.setSolidColor(mainTC);
        btPlay.setPauseLineColor(mainTC);

        sbSongProgress.setRippleColor(vicTC);
        sbSongProgress.setScrubberColor(mainTC);
        sbSongProgress.setThumbColor(vicTC, mainTC);
        sbSongProgress.setTrackColor(vicTC);

    }

    /**
     * 更新数值和文字<br>
     * 1 进度条当前进度和最大值<br>
     * 2 进度条左右文字（进度，总时长）<br>
     * 3 歌曲名，艺术家<br>
     */
    void updateText(int duration, int progress, String title, String arts) {

        tvDuration.setText(StringUtils.getGenTimeMS(duration));
        tvPlayProgress.setText(StringUtils.getGenTimeMS(progress));

        sbSongProgress.setMax(duration);
        sbSongProgress.setProgress(progress);

        tsSongName.setText(title);
        tsSongArts.setText(arts);
    }

    /**
     * 更新播放按钮状态
     */
    void updatePlayButtonStatus(boolean playing) {
        btPlay.setChecked(playing);
    }

    void startProgressUpdateTask() {
        periodicTask.start();
    }

    void stopProgressUpdateTask() {
        periodicTask.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_pre_song:
                try {
                    control.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_next_song:
                try {
                    control.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.play_song:

                try {
                    int stat = control.status();
                    if (stat == PlayController.STATUS_PLAYING) {
                        control.pause();
                    } else {
                        control.resume();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

}
