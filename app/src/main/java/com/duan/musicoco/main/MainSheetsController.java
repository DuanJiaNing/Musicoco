package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.MediaManager;
import com.duan.musicoco.app.SongInfo;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by DuanJiaNing on 2017/7/11.
 */

public class MainSheetsController implements
        View.OnClickListener,
        OnContentUpdate,
        OnEmptyMediaLibrary {

    private TextView mTextAll;
    private TextView mTextRecent;
    private TextView mTextFavorite;

    private TextView mCountAll;
    private TextView mCountRecent;
    private TextView mCountFavorite;

    private ImageView mImageAll;
    private ImageView mImageRecent;
    private ImageView mImageFavorite;

    private View mContainerAll;
    private View mContainerRecent;
    private View mContainerFavorite;

    private Activity activity;
    private DBMusicocoController dbController;
    private final MediaManager mediaManager;

    private boolean hasInitData = false;

    public MainSheetsController(Activity activity, MediaManager mediaManager) {
        this.activity = activity;
        this.mediaManager = mediaManager;
    }

    public void initView() {
        mTextAll = (TextView) activity.findViewById(R.id.sheet_all_text);
        mTextRecent = (TextView) activity.findViewById(R.id.sheet_recent_text);
        mTextFavorite = (TextView) activity.findViewById(R.id.sheet_favorite_text);

        mImageAll = (ImageView) activity.findViewById(R.id.sheet_all_image);
        mImageRecent = (ImageView) activity.findViewById(R.id.sheet_recent_image);
        mImageFavorite = (ImageView) activity.findViewById(R.id.sheet_favorite_image);

        mCountAll = (TextView) activity.findViewById(R.id.sheet_all_count);
        mCountRecent = (TextView) activity.findViewById(R.id.sheet_recent_count);
        mCountFavorite = (TextView) activity.findViewById(R.id.sheet_favorite_count);

        mContainerAll = activity.findViewById(R.id.sheet_all_container);
        mContainerRecent = activity.findViewById(R.id.sheet_recent_container);
        mContainerFavorite = activity.findViewById(R.id.sheet_favorite_container);
        mContainerAll.setOnClickListener(this);
        mContainerRecent.setOnClickListener(this);
        mContainerFavorite.setOnClickListener(this);

    }

    public void initData(DBMusicocoController controller) {
        this.dbController = controller;

        mContainerAll.setClickable(true);
        mContainerRecent.setClickable(true);
        mContainerFavorite.setClickable(true);

        update(null);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        //TODO
        switch (v.getId()) {
            case R.id.sheet_all_container:

                break;
            case R.id.sheet_recent_container:

                break;
            case R.id.sheet_favorite_container:

                break;
        }
    }

    @Override
    public void emptyMediaLibrary() {
        mContainerAll.setClickable(false);
        mContainerRecent.setClickable(false);
        mContainerFavorite.setClickable(false);
    }

    @Override
    public void update(Object obj) {

        List<DBMusicocoController.SongInfo> all = dbController.getSongInfos();

        updateAll(all);
        updateRecent(all);
        updateFavorite(all);

    }

    private void updateFavorite(List<DBMusicocoController.SongInfo> all) {
        Bitmap bitmap = null;
        int count = 0;
        if (all.size() == 0) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_sheet_favorite);
        } else {
            List<DBMusicocoController.SongInfo> favorite = new ArrayList<>();
            for (DBMusicocoController.SongInfo i : all) {
                if (i.favorite) {
                    favorite.add(i);
                }
            }

            count = favorite.size();
            TreeSet<DBMusicocoController.SongInfo> treeSet = dbController.descSortByLastPlayTime(favorite);
            bitmap = findBitmap(treeSet);
        }

        if (bitmap == null) {
            bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet_favorite, mImageFavorite.getWidth(), mImageFavorite.getHeight());
        }

        mImageFavorite.setImageBitmap(createImage(bitmap));
        updateText(mTextFavorite, bitmap, count, mCountFavorite);
    }

    private Bitmap findBitmap(TreeSet<DBMusicocoController.SongInfo> treeSet) {
        Bitmap bitmap = null;
        Iterator<DBMusicocoController.SongInfo> it = treeSet.iterator();
        while (it.hasNext()) {
            DBMusicocoController.SongInfo s = it.next();
            SongInfo info = mediaManager.getSongInfo(s.path);
            bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), mImageRecent.getWidth(), mImageRecent.getHeight());
            if (bitmap != null) {
                break;
            }
        }
        return bitmap;
    }

    private void updateRecent(List<DBMusicocoController.SongInfo> all) {
        Bitmap bitmap = null;
        if (all.size() == 0) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_sheet);
        } else {
            TreeSet<DBMusicocoController.SongInfo> treeSet = dbController.descSortByLastPlayTime(all);
            bitmap = findBitmap(treeSet);
        }

        if (bitmap == null) {
            bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet, mImageRecent.getWidth(), mImageRecent.getHeight());
        }

        mImageRecent.setImageBitmap(createImage(bitmap));
        updateText(mTextRecent, bitmap, all.size(), mCountRecent);

    }

    private void updateAll(List<DBMusicocoController.SongInfo> all) {
        Bitmap bitmap = null;

        if (all.size() == 0) {
            bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_sheet);
        } else {
            for (DBMusicocoController.SongInfo i : all) {
                SongInfo info = mediaManager.getSongInfo(i.path);
                bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), mImageAll.getWidth(), mImageAll.getHeight());
                if (bitmap != null) {
                    break;
                }
            }
        }

        if (bitmap == null) {
            bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet, mImageAll.getWidth(), mImageAll.getHeight());
        }

        mImageAll.setImageBitmap(createImage(bitmap));
        updateText(mTextAll, bitmap, all.size(), mCountAll);

    }

    private void updateText(TextView categoryView, Bitmap bitmap, int count, TextView countView) {
        int defaultColor = Color.GRAY;
        int defaultTextColor = Color.DKGRAY;
        int[] colors = new int[4];
        ColorUtils.get4LightColorWithTextFormBitmap(bitmap, defaultColor, defaultTextColor, colors);

        GradientDrawable categoryD = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{colors[0], colors[2]}
        );
        categoryView.setBackground(categoryD);
        categoryView.setTextColor(colors[1]);

        GradientDrawable countD = new GradientDrawable();
        countD.setColor(colors[2]);
        countD.setCornerRadius(countView.getHeight() / 2);
        countView.setBackground(countD);
        countView.setTextColor(colors[1]);

        int c = count;
        if (countView == mCountRecent) {
            int recent = activity.getResources().getInteger(R.integer.sheet_recent_count);
            c = count > recent ? recent : c;
        }
        countView.setText(String.valueOf(c));

    }

    private Bitmap createImage(Bitmap bitmap) {
        return bitmap;
    }

    public boolean hasInitData() {
        return hasInitData;
    }
}
