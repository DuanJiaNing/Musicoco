package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.image.BitmapBuilder;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;

import java.util.List;

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
        mInfoContainer.setOnClickListener(this);
    }

    public void initData(@NonNull DBMusicocoController dbMusicoco, @NonNull String title) {
        this.dbMusicoco = dbMusicoco;

        mShowMore.setEnabled(true);
        mShowMore.setClickable(true);
        mInfoContainer.setEnabled(true);
        mInfoContainer.setClickable(true);

        update(title);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        //TODO 完成显示更多
        switch (v.getId()) {
            case R.id.rmp_see_more:
                break;
            case R.id.rmp_info_container:

                break;
        }

    }

    @Override
    public void emptyMediaLibrary() {

        mShowMore.setEnabled(true);
        mShowMore.setClickable(true);
        mInfoContainer.setEnabled(true);
        mInfoContainer.setClickable(true);

        mName.setText("");
        mArts.setText("");
        mRemark.setText("");
        mPlayTime.setText(String.valueOf(0));
    }

    @Override
    public void update(Object obj) {
        List<DBMusicocoController.SongInfo> list = dbMusicoco.getSongInfos();

        int maxPlayTime = 0;
        String path = "";
        String remark = "";
        for (DBMusicocoController.SongInfo s : list) {
            int time = s.playTimes;
            if (time > maxPlayTime) {
                maxPlayTime = time;
                path = s.path;
                remark = s.remark;
            }
        }

        Song song = new Song(path);
        SongInfo info = mediaManager.getSongInfo(song);

        String type = (obj != null && obj instanceof CharSequence) ? obj.toString() : "";
        updateText(info, remark, maxPlayTime, type);

        if (currentSong == null || !currentSong.equals(song)) {
            updateImage(info);
            currentSong = song;
        }

    }

    private void updateText(SongInfo info, String remark, int maxPlayTime, String type) {

        String name = info.getTitle();
        String arts = info.getArtist();

        mName.setText(name);
        mArts.setText(arts);
        mRemark.setText(remark);
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

    private void updateImage(SongInfo info) {

        Bitmap bitmap = null;
        if (info.getAlbum_path() != null) {
            BitmapBuilder builder = new BitmapBuilder(activity);
            bitmap = builder.setPath(info.getAlbum_path())
                    .resize(mImage.getWidth(), mImage.getHeight())
                    .build().getBitmap();
        }

        if (bitmap != null) {
            mImage.setImageBitmap(bitmap);
        } else {
            bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(),
                    R.drawable.default_album,
                    mImage.getWidth(),
                    mImage.getHeight());
            mImage.setImageBitmap(bitmap);
        }

        AlphaAnimation anim = new AlphaAnimation(0.4f, 1.0f);
        anim.setDuration(1000);
        mImage.startAnimation(anim);

        int[] colors = new int[6];
        int defaultColor = activity.getResources().getColor(R.color.rmp_image_default_bg);
        ColorUtils.get6ColorFormBitmap(bitmap, defaultColor, colors);
        GradientDrawable drawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{colors[4], colors[5]});
        mContainer.setBackground(drawable);

        drawable.setAlpha(180);
        drawable.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        mInfoContainer.setBackground(drawable);

    }

    @Override
    public void themeChange(Theme theme, int[] colors) {

        int[] cs = new int[4];

        switch (theme) {
            case DARK: {
                cs = ColorUtils.get4DarkThemeColors(activity);
                int mainTC = cs[1];
                int vicTC = cs[3];
                mName.setTextColor(mainTC);
                // E/rsC++: RS CPP error: Blur radius out of 0-25 pixel bound
                // radius 不能大于 25，模拟器没问题，但真机无法运行
                mName.setShadowLayer(20, 0, 0, mainTC);
                mRemark.setTextColor(vicTC);
                mArts.setTextColor(vicTC);
                mInfoLine.setBackgroundColor(mainTC);
                break;
            }
            case WHITE:
            default: {
                cs = ColorUtils.get4WhiteThemeColors(activity);
                int mainBC = cs[0];
                int vicBC = cs[2];
                mName.setTextColor(mainBC);
                mName.setShadowLayer(20, 0, 0, mainBC);
                mRemark.setTextColor(vicBC);
                mArts.setTextColor(vicBC);
                mInfoLine.setBackgroundColor(mainBC);
                break;
            }
        }

        int mainTC = cs[1];
        int vicTC = cs[3];

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
