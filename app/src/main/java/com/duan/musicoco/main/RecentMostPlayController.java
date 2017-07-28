package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.DBSongInfo;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/9.
 */

public class RecentMostPlayController implements
        View.OnClickListener,
        OnContentUpdate,
        OnEmptyMediaLibrary,
        OnThemeChange {

    private TextView mType;
    private TextView mTypeLine;

    private TextView mName;
    private TextView mArts;
    private TextView mRemark;

    private TextView mPlayTime;
    private TextView mPlayTimeL;
    private TextView mPlayTimeR;

    private TextView mShowMore;

    private View mLine;
    private View mInfoLine;

    private ImageView mImage;
    private View mInfoContainer;
    private View mContainer;

    private final Activity activity;
    private DBMusicocoController dbMusicoco;
    private final MediaManager mediaManager;
    private boolean hasInitData = false;

    private Song currentSong;

    public RecentMostPlayController(Activity activity, MediaManager manager) {
        this.activity = activity;
        this.mediaManager = manager;
    }

    public void initView() {
        mType = (TextView) activity.findViewById(R.id.rmp_type);
        mTypeLine = (TextView) activity.findViewById(R.id.rmp_type_line);

        mName = (TextView) activity.findViewById(R.id.rmp_info_name);
        mArts = (TextView) activity.findViewById(R.id.rmp_info_arts);
        mRemark = (TextView) activity.findViewById(R.id.rmp_info_remark);

        mPlayTime = (TextView) activity.findViewById(R.id.rmp_play_time);
        mPlayTimeL = (TextView) activity.findViewById(R.id.rmp_l_time);
        mPlayTimeR = (TextView) activity.findViewById(R.id.rmp_r_time);

        mShowMore = (TextView) activity.findViewById(R.id.rmp_see_more);
        mLine = activity.findViewById(R.id.rmp_line);
        mInfoLine = activity.findViewById(R.id.rmp_info_line);
        mImage = (ImageView) activity.findViewById(R.id.rmp_image);
        mInfoContainer = activity.findViewById(R.id.rmp_info_container);
        mContainer = activity.findViewById(R.id.rmp_container);

        mShowMore.setOnClickListener(this);
    }

    public void initData(@NonNull DBMusicocoController dbMusicoco, @NonNull String title) {
        this.dbMusicoco = dbMusicoco;

        mShowMore.setEnabled(true);
        mShowMore.setClickable(true);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        //TODO 完成显示更多
        switch (v.getId()) {
            case R.id.rmp_see_more:
                break;
        }
    }

    @Override
    public void emptyMediaLibrary() {

        mShowMore.setEnabled(true);
        mShowMore.setClickable(true);

        mName.setText("");
        mArts.setText("");
        mRemark.setText("");
        mPlayTime.setText(String.valueOf(0));
    }

    private class Data {
        SongInfo info;
        String remark;
        int maxPlayTime;
        String type;
        Bitmap bitmap = null;
        int[] colors = null;
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {
        Log.d("update", "RecentMostPlayController update");

        final Object ob = obj;
        Observable.OnSubscribe<Data> onSubscribe = new Observable.OnSubscribe<Data>() {
            @Override
            public void call(Subscriber<? super Data> subscriber) {

                List<DBSongInfo> list = dbMusicoco.getSongInfos();
                int maxPlayTime = 0;
                String path = "";
                String remark = "";
                for (DBSongInfo s : list) {
                    int time = s.playTimes;
                    if (time > maxPlayTime) {
                        maxPlayTime = time;
                        path = s.path;
                        remark = s.remark;
                    }
                }
                Song song = new Song(path);
                SongInfo info = mediaManager.getSongInfo(song);

                Data data = new Data();
                data.info = info;
                data.remark = remark;
                data.maxPlayTime = maxPlayTime;
                data.type = (ob != null && ob instanceof CharSequence) ? ob.toString() : "";

                if (currentSong == null || !currentSong.equals(song)) {
                    Bitmap bitmap = null;
                    if (info.getAlbum_path() != null) {
                        BitmapBuilder builder = new BitmapBuilder(activity);
                        bitmap = builder.setPath(info.getAlbum_path())
                                .resize(mImage.getWidth(), mImage.getHeight())
                                .build().getBitmap();
                    }

                    if (bitmap == null) {
                        bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(),
                                R.drawable.default_album,
                                mImage.getWidth(),
                                mImage.getHeight());
                    }
                    data.bitmap = bitmap;

                    //更新当前 XX 最多播放曲目
                    currentSong = song;
                } else {
                    data.bitmap = null;
                }

                if (data.bitmap != null) {
                    data.colors = new int[4];
                    int defaultColor = activity.getResources().getColor(R.color.rmp_image_default_bg);
                    int defaultTextColor = Color.GRAY;
                    ColorUtils.get4LightColorWithTextFormBitmap(data.bitmap, defaultColor, defaultTextColor, data.colors);
                } else {
                    data.colors = null;
                }

                subscriber.onNext(data);
                subscriber.onCompleted();
            }
        };

        Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Data>() {
                    @Override
                    public void call(Data data) {
                        updateText(data.info, data.remark, data.maxPlayTime, data.type);

                        if (data.bitmap != null) {
                            updateImage(data.bitmap);
                        }

                        if (data.colors != null) {
                            updateColors(data.colors);
                        }
                    }
                });

    }

    private void updateImage(@NonNull Bitmap bitmap) {
        Log.d("update", "RecentMostPlayController updateImage");
        mImage.setImageBitmap(bitmap);
        AlphaAnimation anim = new AlphaAnimation(0.4f, 1.0f);
        anim.setDuration(1000);
        mImage.startAnimation(anim);
    }

    private void updateText(SongInfo info, String remark, int maxPlayTime, String type) {
        Log.d("update", "RecentMostPlayController updateText");

        String name = info.getTitle();
        String arts = info.getArtist();

        mName.setText(name);
        mArts.setText(arts);
        mRemark.setText("  " + remark);
        mPlayTime.setText(String.valueOf(maxPlayTime));

        mName.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) mInfoLine.getLayoutParams();
                p.width = mName.getWidth() * 2 / 3;
                mInfoLine.setLayoutParams(p);
            }
        });

        mType.setText(type);
    }

    private void updateColors(@NonNull int[] colors) {
        Log.d("update", "RecentMostPlayController updateColors");

        int startC = colors[0];
        int endC = colors[2];

        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startC, endC});
        mContainer.setBackground(drawable);

        drawable.setAlpha(170);
        int colorFilter = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorFilter = activity.getColor(R.color.main_rmp_image_filter);
        } else {
            colorFilter = activity.getResources().getColor(R.color.main_rmp_image_filter);
        }

        drawable.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
        mInfoContainer.setBackground(drawable);


        int textC = colors[3];
        int textBC = colors[0];
        GradientDrawable td = new GradientDrawable();
        td.setColor(textBC);
        td.setCornerRadius(mArts.getHeight() / 2);
        mArts.setBackground(td);
        mArts.setTextColor(textC);

    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        int[] cs;
        switch (theme) {
            case DARK: {
                cs = ColorUtils.get10DarkThemeColors(activity);
                break;
            }
            case WHITE:
            default: {
                cs = ColorUtils.get10WhiteThemeColors(activity);
                break;
            }
        }

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

        mName.setTextColor(toolbarMainTC);
        // E/rsC++: RS CPP error: Blur radius out of 0-25 pixel bound
        // radius 不能大于 25，模拟器没问题，但真机无法运行
        mName.setShadowLayer(20, 0, 0, toolbarMainTC);
        mRemark.setTextColor(toolbarVicTC);
        mArts.setTextColor(vicTC);
        mInfoLine.setBackgroundColor(mainTC);

        mPlayTimeL.setTextColor(vicTC);
        mPlayTimeR.setTextColor(vicTC);
        mPlayTime.setTextColor(mainTC);

        mShowMore.setTextColor(vicTC);
        mLine.setBackgroundColor(vicTC);
        mType.setTextColor(mainTC);
        mTypeLine.setBackgroundColor(mainTC);

    }

    public boolean hasInitData() {
        return hasInitData;
    }
}
