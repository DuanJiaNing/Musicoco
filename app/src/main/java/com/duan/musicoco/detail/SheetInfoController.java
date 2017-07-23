package com.duan.musicoco.detail;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.shared.SheetCoverHelper;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.Utils;
import com.duan.musicoco.view.AppBarStateChangeListener;

/**
 * Created by DuanJiaNing on 2017/7/23.
 */

public class SheetInfoController implements View.OnClickListener {

    private Activity activity;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private FloatingActionButton fabPlayAll;
    private ImageView imageView;

    private String title;
    private String createTime;

    private TextView tvName;
    private TextView tvCreateTime;

    private SheetCoverHelper sheetCoverHelper;

    private int mainBC;
    private int vicBC;
    private int mainTC;
    private int vicTC;

    private final SheetCoverHelper.OnFindCompleted onFindCompleted;
    private final AppBarStateChangeListener barStateChangeListener;

    public SheetInfoController(Activity activity) {
        this.activity = activity;
        this.onFindCompleted = new SheetCoverHelper.OnFindCompleted() {
            @Override
            public void completed(Bitmap bitmap) {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    initColor(bitmap);
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

    public void initView() {

        appBarLayout = (AppBarLayout) activity.findViewById(R.id.sheet_detail_app_bar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.sheet_detail_toolbar_layout);
        toolbar = (Toolbar) activity.findViewById(R.id.sheet_detail_toolbar);
        fabPlayAll = (FloatingActionButton) activity.findViewById(R.id.sheet_detail_play_all);
        tvName = (TextView) activity.findViewById(R.id.sheet_detail_name);
        tvCreateTime = (TextView) activity.findViewById(R.id.sheet_detail_create_time);
        imageView = (ImageView) activity.findViewById(R.id.sheet_detail_image);

        fabPlayAll.setOnClickListener(this);

    }

    public void initData(boolean isMainSheet, Sheet sheet, int sheetID, DBMusicocoController dbController, MediaManager mediaManager) {

        if (isMainSheet) {
            title = MainSheetHelper.getMainSheetName(activity, sheetID);
            createTime = "";
        } else {
            title = activity.getString(R.string.create_time) + ": " + sheet.name;
            createTime = StringUtils.getGenDateYMD(sheet.create);
        }

        initText();
        initImage(sheetID, dbController, mediaManager);

        appBarLayout.addOnOffsetChangedListener(barStateChangeListener);
    }

    private void initImage(int id, DBMusicocoController dbController, MediaManager mediaManager) {
        int imageSize = Utils.getMetrics(activity).widthPixels;
        sheetCoverHelper = new SheetCoverHelper(
                activity,
                dbController,
                mediaManager,
                imageSize, imageSize
        );
        sheetCoverHelper.find(onFindCompleted, id);
    }

    private void initColor(Bitmap bitmap) {

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
        mainBC = colors[0];
        mainTC = colors[1];
        vicBC = colors[2];
        vicTC = colors[3];

        tvName.setTextColor(mainTC);
        tvCreateTime.setTextColor(vicTC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            GradientDrawable drawable = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{mainBC});
            drawable.setAlpha(230);
            imageView.setForeground(drawable);
        }

        fabPlayAll.setBackgroundTintList(ColorStateList.valueOf(mainBC));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fabPlayAll.getDrawable().setTint(mainTC);
        }
        fabPlayAll.setRippleColor(vicTC);

    }

    private void initText() {
        tvName.setText(title);
        tvCreateTime.setText(createTime);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sheet_detail_play_all:

                break;
        }
    }

}
