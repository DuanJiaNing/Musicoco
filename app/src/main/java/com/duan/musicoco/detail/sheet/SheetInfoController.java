package com.duan.musicoco.detail.sheet;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.SheetCoverHelper;
import com.duan.musicoco.util.StringUtils;

import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

/**
 * Created by DuanJiaNing on 2017/7/23.
 */

public class SheetInfoController implements ThemeChangeable {

    private final boolean isDarkThem;
    private Activity activity;
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
    private Sheet sheet;

    public SheetInfoController(Activity activity, boolean darkTheme) {
        this.activity = activity;
        this.isDarkThem = darkTheme;
        this.onFindCompleted = new SheetCoverHelper.OnFindCompleted() {
            @Override
            public void completed(SongInfo info) {
                initImages(info);
            }
        };
    }

    public void initView() {

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

    }

    public String getTitle() {
        return title;
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

    private void initImages(SongInfo info) {

        if (info != null && !TextUtils.isEmpty(info.getAlbum_path())) {
            String path = info.getAlbum_path();

            Glide.with(activity)
                    .load(path)
                    .into(imageView);

            Glide.with(activity)
                    .load(path)
                    .bitmapTransform(new VignetteFilterTransformation(activity))
                    .into(imageViewBG);
        } else {
            int id;
            switch (sheetID) {
                case MainSheetHelper.SHEET_RECENT:
                    id = R.drawable.default_sheet_recent;
                    break;
                case MainSheetHelper.SHEET_FAVORITE:
                    id = R.drawable.default_sheet_favorite;
                    break;
                case MainSheetHelper.SHEET_ALL:
                default:
                    id = R.drawable.default_sheet_all;
                    break;
            }
            Glide.with(activity)
                    .load(id)
                    .into(imageView);

            Glide.with(activity)
                    .load(id)
                    .bitmapTransform(new VignetteFilterTransformation(activity))
                    .into(imageViewBG);
        }


        if (isDarkThem) {
            imageViewBG.setAlpha(0.3f);
        }

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        int mainTC = colors[0];
        int vicTC = colors[1];

        name.setTextColor(mainTC);
        name.setShadowLayer(20.0f, 0, 0, mainTC);

        remark.setTextColor(vicTC);
        createTime.setTextColor(vicTC);

    }

    public void updateFloatingBtColor(int[] colors) {

        int tintC = colors[0];
        int rippleC = colors[1];
        int bgC = colors[2];

        fabPlayAll.setBackgroundTintList(ColorStateList.valueOf(bgC));
        fabPlayAll.setRippleColor(rippleC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fabPlayAll.getDrawable().setTint(tintC);
        }
    }
}
