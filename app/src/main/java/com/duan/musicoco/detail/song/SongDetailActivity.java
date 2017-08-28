package com.duan.musicoco.detail.song;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.On2CompleteListener;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class SongDetailActivity extends RootActivity implements View.OnClickListener {

    private ImageButton mClose;
    private FloatingActionButton mSaveImage;
    private ImageView mImage;
    private TextView mName;
    private TextView mArts;
    private View mLine;
    private TextView mInfos;
    private View container;

    private SongInfo info;
    private MediaManager mediaManager;

    private boolean haveAlbumImage = false;
    private boolean startFromPlayActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_detail_activity);

        Utils.transitionStatusBar(this);

        initViews();

        Intent intent = getIntent();
        String path = intent.getExtras().get(ActivityManager.SONG_DETAIL_PATH).toString();
        startFromPlayActivity = intent.getExtras().getBoolean(ActivityManager.SONG_DETAIL_START_FROM_PLAY_ACTIVITY, false);
        Song song = new Song(path);

        mediaManager = MediaManager.getInstance();

        info = mediaManager.getSongInfo(this, song);

        if (info != null) {
            initText();
            mImage.post(new Runnable() {
                @Override
                public void run() {
                    initImageAndColor();
                }
            });
        }

    }

    @Override
    protected void checkTheme() {
        // 不需要检查主题
    }

    private void initText() {

        mName.setText(info.getTitle());
        mArts.setText(info.getArtist());
        mInfos.setText(getInfos());
    }

    private String getInfos() {

        String duration = StringUtils.getGenTimeMS((int) info.getDuration());
        String album = info.getAlbum();
        String year = StringUtils.getGenDateYMDHMS(info.getYear());
        String path = info.getData();
        String size = String.valueOf(info.getSize() >> 10 >> 10) + " MB";
        String dateAdded = StringUtils.getGenDateYMDHMS(info.getDate_added());
        String mimeType = info.getMime_type();

        DBSongInfo songInfo = dbController.getSongInfo(new Song(path));
        String playTimes = songInfo.playTimes + " " + getString(R.string.count);
        String remark = songInfo.remark;
        String lastPlayTime = StringUtils.getGenDateYMDHMS(songInfo.lastPlayTime);
        String favorite = songInfo.favorite ? getString(R.string.song_detail_favorite) : getString(R.string.song_detail_not_favorite);

        int[] ss = songInfo.sheets;
        StringBuilder sheets = new StringBuilder();
        for (int i = 0; i < ss.length; i++) {
            Sheet s = dbController.getSheet(ss[i]);
            if (s != null) {
                sheets.append(s.name);
                if (i != ss.length - 1) {
                    sheets.append("、");
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        String nl2 = "\n\n";
        String nl1 = "\n";
        builder.append(remark).append(nl1)
                .append(getString(R.string.song_detail_duration)).append(": ").append(duration).append(nl1)
                .append(getString(R.string.song_detail_album)).append(": ").append(album).append(nl1)
                .append(getString(R.string.song_detail_year)).append(": ").append(year).append(nl1)
                .append(getString(R.string.song_detail_path)).append(": ").append(path).append(nl1)
                .append(getString(R.string.song_detail_size)).append(": ").append(size).append(nl1)
                .append(getString(R.string.song_detail_add_time)).append(": ").append(dateAdded).append(nl1)
                .append(getString(R.string.song_detail_mimeType)).append(": ").append(mimeType).append(nl2)
                .append(getString(R.string.song_detail_play_times)).append(": ").append(playTimes).append(nl1)
                .append(getString(R.string.song_detail_last_play)).append(": ").append(lastPlayTime).append(nl1)
                .append(getString(R.string.song_detail_favorite)).append(" ? ").append(favorite).append(nl2)
                .append(getString(R.string.song_detail_song_list)).append(": ").append(nl1).append(sheets.toString()).append(nl2);

        return builder.toString();
    }

    private void initImageAndColor() {

        Bitmap bitmap = null;
        String ip = info.getAlbum_path();
        if (!TextUtils.isEmpty(ip)) {
            bitmap = BitmapUtils.bitmapResizeFromFile(ip, mImage.getWidth(), mImage.getHeight());
            haveAlbumImage = true;
        }

        if (bitmap == null) {
            bitmap = BitmapUtils.getDefaultPictureForAlbum(this, mImage.getWidth(), mImage.getHeight());
            haveAlbumImage = false;
        }

        mImage.setImageBitmap(bitmap);

        int dc = Color.GRAY;
        int dtc = Color.BLACK;
        int[] colors = new int[4];
        ColorUtils.get4LightColorWithTextFormBitmap(bitmap, dc, dtc, colors);
        int mainBC = colors[0];
        int mainTC = colors[1];
        int vicBC = colors[2];
        int vicTC = colors[3];

        GradientDrawable dm = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{mainBC, vicBC});
        container.setBackground(dm);

        mLine.setBackgroundColor(vicTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mClose.getDrawable().setTint(mainTC);
        }

        mSaveImage.setBackgroundTintList(ColorStateList.valueOf(mainBC));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mSaveImage.getDrawable().setTint(vicTC);
        }
        mSaveImage.setRippleColor(vicTC);

        mName.setTextColor(mainTC);
        mArts.setTextColor(mainTC);
        mInfos.setTextColor(mainTC);

    }

    private void initViews() {

        container = findViewById(R.id.song_detail_container);

        mClose = (ImageButton) findViewById(R.id.song_detail_close);
        mSaveImage = (FloatingActionButton) findViewById(R.id.song_detail_save_image);

        mImage = (ImageView) findViewById(R.id.song_detail_image);
        mName = (TextView) findViewById(R.id.song_detail_name);
        mArts = (TextView) findViewById(R.id.song_detail_arts);
        mLine = findViewById(R.id.song_detail_ni_line);
        mInfos = (TextView) findViewById(R.id.song_detail_infos);

        mImage.setOnClickListener(this);
        mClose.setOnClickListener(this);
        mSaveImage.setOnClickListener(this);

    }

    @Override
    public void onBackPressed() {
        if (startFromPlayActivity) {
            ActivityManager.getInstance().startPlayActivity(this);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.song_detail_close:
                onBackPressed();
                break;

            case R.id.song_detail_save_image: {
                if (haveAlbumImage) {
                    String path = info.getAlbum_path();
                    FileUtils.saveImage(this, path, new On2CompleteListener<Boolean, String>() {
                        @Override
                        public void onComplete(Boolean aBoolean, String s) {
                            String msg;
                            if (aBoolean) {
                                msg = getString(R.string.success_save_image_to) + s;
                            } else {
                                msg = getString(R.string.error_save_fail);
                            }
                            ToastUtils.showShortToast(msg, SongDetailActivity.this);
                        }
                    });
                } else {
                    ToastUtils.showShortToast(getString(R.string.error_no_album_image), this);
                }
                break;
            }
            case R.id.song_detail_image: {
                if (haveAlbumImage) {
                    String path = info.getAlbum_path();
                    ActivityManager.getInstance().startImageCheckActivity(this, path);
                } else {
                    ToastUtils.showShortToast(getString(R.string.error_no_album_image), this);
                }
                break;
            }
        }
    }
}
