package com.duan.musicoco.detail;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.BoringLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.shared.SheetCoverHelper;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.view.AppBarStateChangeListener;

/**
 * Created by DuanJiaNing on 2017/7/23.
 */

public class SheetInfoController {

    private Activity activity;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fabPlayAll;
    private ImageView imageViewBG;
    private ImageView imageView;
    private TextView name;
    private TextView remark;
    private TextView createTime;

    private SheetCoverHelper sheetCoverHelper;

    private int sheetID;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;
    private String title;

    private final SheetCoverHelper.OnFindCompleted onFindCompleted;
    private final AppBarStateChangeListener barStateChangeListener;
    private Sheet sheet;

    public SheetInfoController(Activity activity) {
        this.activity = activity;
        this.onFindCompleted = new SheetCoverHelper.OnFindCompleted() {
            @Override
            public void completed(SongInfo info) {
                if (info != null) {
                    initImages(info.getAlbum_path());
                    initColor(info.getAlbum_path());
                }
            }
        };
        barStateChangeListener = new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case EXPANDED:
                        collapsingToolbarLayout.setTitle(" ");
                        break;
                    case COLLAPSED:
                        collapsingToolbarLayout.setTitle(title);
                        break;
                    case IDLE:
                        collapsingToolbarLayout.setTitle(" ");
                        break;
                }
            }
        };
    }

    private void initImages(String path) {
        Glide.with(activity).load(path).into(imageView);
        Glide.with(activity).load(path).into(imageViewBG);
    }

    public void initView() {

        appBarLayout = (AppBarLayout) activity.findViewById(R.id.sheet_detail_app_bar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.sheet_detail_toolbar_layout);
        toolbar = (Toolbar) activity.findViewById(R.id.sheet_detail_toolbar);
        fabPlayAll = (FloatingActionButton) activity.findViewById(R.id.sheet_detail_play_all);
        imageViewBG = (ImageView) activity.findViewById(R.id.sheet_detail_image_bg);
        imageView = (ImageView) activity.findViewById(R.id.sheet_detail_image);
        name = (TextView) activity.findViewById(R.id.sheet_detail_name);
        remark = (TextView) activity.findViewById(R.id.sheet_detail_remark);
        createTime = (TextView) activity.findViewById(R.id.sheet_detail_create_time);
    }

    public void initData(int sheetID, Sheet sheet,
                         DBMusicocoController dbController, MediaManager mediaManager) {
        this.sheetID = sheetID;
        this.sheet = sheet;
        this.dbController = dbController;
        this.mediaManager = mediaManager;

        initTexts();
        initImageAndColor();

        appBarLayout.addOnOffsetChangedListener(barStateChangeListener);
    }

    private void initTexts() {
        if (sheetID < 0) {
            title = MainSheetHelper.getMainSheetName(activity, sheetID);
            name.setText(title);
            remark.setVisibility(View.GONE);
            createTime.setText("");
        } else if (sheet != null) {
            title = sheet.name;
            name.setText(title);
            remark.setText(sheet.remark);
            String ct = activity.getString(R.string.create_time) + ": " + StringUtils.getGenDateYMD(sheet.create);
            createTime.setText(ct);
        }
    }

    private void initImageAndColor() {
        sheetCoverHelper = new SheetCoverHelper(
                activity,
                dbController,
                mediaManager
        );
        sheetCoverHelper.find(onFindCompleted, sheetID);
    }

    private void initColor(String path) {

        BitmapDrawable bd = (BitmapDrawable) imageViewBG.getDrawable();
        Bitmap bitmap;
        if (bd == null) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            bitmap = bd.getBitmap();
        }

        if (bitmap != null) {

            int[] colors = new int[4];
            int dbc;
            int dtc;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dbc = activity.getColor(R.color.colorPrimary);
                dtc = activity.getColor(R.color.white_d);
            } else {
                dbc = activity.getResources().getColor(R.color.colorPrimary);
                dtc = activity.getResources().getColor(R.color.white_d);
            }

            ColorUtils.get4LightColorWithTextFormBitmap(bitmap, dbc, dtc, colors);
            int mainBC = colors[0];
            int mainTC = colors[1];
            int vicBC = colors[2];
            int vicTC = colors[3];

            fabPlayAll.setBackgroundTintList(ColorStateList.valueOf(mainBC));
            fabPlayAll.setRippleColor(vicTC);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fabPlayAll.getDrawable().setTint(mainTC);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{mainBC, vicBC}
                );
                gd.setAlpha(245);
                imageViewBG.setForeground(gd);
            }

            boolean lightBG = ColorUtils.isBrightSeriesColor(mainBC);
            int mTC;
            int vTC;
            if (lightBG) {
                vTC = activity.getResources().getColor(R.color.white);
                mTC = activity.getResources().getColor(R.color.white_d);
            } else {
                vTC = activity.getResources().getColor(R.color.dark_l_l_l_l);
                mTC = activity.getResources().getColor(R.color.dark_l_l_l);
            }
            name.setTextColor(mTC);
            remark.setTextColor(vTC);
            createTime.setTextColor(vTC);

        }
    }

}
