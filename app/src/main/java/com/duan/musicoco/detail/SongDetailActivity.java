package com.duan.musicoco.detail;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.DBSongInfo;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.FileUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;

/**
 * Created by DuanJiaNing on 2017/7/19.
 */

public class SongDetailActivity extends AppCompatActivity implements View.OnClickListener {

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
    private DBMusicocoController dbMusicocoController;

    private boolean haveAlbumImage = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_detail_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        initViews();

        Intent intent = getIntent();
        String path = intent.getExtras().get(ActivityManager.SONG_DETAIL).toString();
        Song song = new Song(path);

        mediaManager = MediaManager.getInstance(this);
        info = mediaManager.getSongInfo(song);

        if (info != null) {
            dbMusicocoController = new DBMusicocoController(this, false);

            initText();
            mImage.post(new Runnable() {
                @Override
                public void run() {
                    initImageAndColor();
                }
            });
        }

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

        DBSongInfo songInfo = dbMusicocoController.getSongInfo(new Song(path));
        String playTimes = songInfo.playTimes + "次";
        String remark = songInfo.remark;
        String lastPlayTime = StringUtils.getGenDateYMDHMS(songInfo.lastPlayTime);
        String favorite = songInfo.favorite ? "已收藏" : "未收藏";

        int[] ss = songInfo.sheets;
        StringBuilder sheets = new StringBuilder();
        for (int i = 0; i < ss.length; i++) {
            Sheet s = dbMusicocoController.getSheet(ss[i]);
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
                .append("时长：").append(duration).append(nl1)
                .append("所属专辑：").append(album).append(nl1)
                .append("年份：").append(year).append(nl1)
                .append("文件路径：").append(path).append(nl1)
                .append("大小：").append(size).append(nl1)
                .append("添加时间：").append(dateAdded).append(nl1)
                .append("格式：").append(mimeType).append(nl2)
                .append("播放次数：").append(playTimes).append(nl1)
                .append("最后播放时间：").append(lastPlayTime).append(nl1)
                .append("收藏？ ").append(favorite).append(nl2)
                .append("已加入歌单：").append(nl1).append(sheets.toString()).append(nl2);

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
            bitmap = BitmapUtils.getDefaultAlbumPicture(this, mImage.getWidth(), mImage.getHeight());
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
        mClose.getDrawable().setTint(mainTC);

        mSaveImage.setBackgroundTintList(ColorStateList.valueOf(mainBC));
        mSaveImage.getDrawable().setTint(vicTC);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.song_detail_close:
                finish();
                break;

            case R.id.song_detail_save_image: {
                if (haveAlbumImage) {
                    String path = info.getAlbum_path();
                    String to = FileUtils.saveAlbumPicture(this, path);
                    ToastUtils.showLongToast(this, to);
                } else {
                    ToastUtils.showShortToast(this, getString(R.string.error_no_album_image));
                }
                break;
            }
            case R.id.song_detail_image: {
                if (haveAlbumImage) {
                    String path = info.getAlbum_path();
                    new ActivityManager(this).startImageCheckActivity(path);
                } else {
                    ToastUtils.showShortToast(this, getString(R.string.error_no_album_image));
                }
                break;
            }
        }
    }
}
