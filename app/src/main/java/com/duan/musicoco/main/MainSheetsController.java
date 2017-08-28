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
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.interfaces.SubscriberAbstract;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by DuanJiaNing on 2017/7/11.
 */

public class MainSheetsController implements
        View.OnClickListener,
        ContentUpdatable {

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

    private MainSheetHelper mainSheetHelper;

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
        this.mainSheetHelper = new MainSheetHelper(activity, dbController);
        hasInitData = true;
    }

    @Override
    public void onClick(View v) {
        ActivityManager manager = ActivityManager.getInstance();
        switch (v.getId()) {
            case R.id.sheet_all_container:
                manager.startSheetDetailActivity(activity, MainSheetHelper.SHEET_ALL, null);
                break;
            case R.id.sheet_recent_container:
                manager.startSheetDetailActivity(activity, MainSheetHelper.SHEET_RECENT, null);
                break;
            case R.id.sheet_favorite_container:
                manager.startSheetDetailActivity(activity, MainSheetHelper.SHEET_FAVORITE, null);
                break;
        }
    }

    private static class Data {
        final static int ALL = 0;
        final static int RECENT = 1;
        final static int FAVORITE = 2;

        Bitmap bitmap;
        int count;
        int[] colors;
        int which;
    }

    @Override
    public void update(Object obj, final OnUpdateStatusChanged statusChanged) {

        Observable.just(Data.ALL, Data.RECENT, Data.FAVORITE)
                .map(new Func1<Integer, Data>() {
                    List<DBSongInfo> all = mainSheetHelper.getAllSongInfo();

                    @Override
                    public Data call(Integer integer) {

                        Data data = new Data();
                        if (all.size() == 0) {
                            data.count = 0;
                            data.bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_sheet);
                        } else {
                            switch (integer) {
                                case Data.ALL:
                                    data = getDataForAll(all);
                                    break;
                                case Data.RECENT:
                                    data = getDataForRecent(all);
                                    break;
                                case Data.FAVORITE:
                                    data = getDataForFavorite();
                                    break;
                                default: {
                                    data.count = 0;
                                    data.bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_sheet);
                                    break;
                                }
                            }
                        }

                        if (data.bitmap == null) {
                            data.bitmap = BitmapUtils.bitmapResizeFromResource(activity.getResources(), R.drawable.default_sheet, mImageRecent.getWidth(), mImageRecent.getHeight());
                        }

                        int[] colors = new int[4];
                        int defaultColor = Color.GRAY;
                        int defaultTextColor = Color.DKGRAY;
                        ColorUtils.get4DarkColorWithTextFormBitmap(
                                data.bitmap,
                                defaultColor,
                                defaultTextColor,
                                colors);
                        data.colors = colors;
                        data.which = integer;
                        return data;
                    }
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SubscriberAbstract<Data>(statusChanged) {
                    @Override
                    public void onNext(Data data) {
                        switch (data.which) {
                            case Data.ALL:
                                updateAll(data);
                                break;
                            case Data.RECENT:
                                updateRecent(data);
                                break;
                            case Data.FAVORITE:
                                updateFavorite(data);
                                break;
                        }
                    }
                });
    }

    @Override
    public void noData() {
    }

    private Data getDataForFavorite() {
        Data data = new Data();
        List<DBSongInfo> favorite = mainSheetHelper.getFavoriteSongInfo();
        List<DBSongInfo> list = DBSongInfo.descSortByLastPlayTime(favorite);
        data.bitmap = findBitmap(list, list.size());
        data.count = favorite.size();
        return data;
    }

    private Data getDataForRecent(List<DBSongInfo> all) {
        Data data = new Data();
        List<DBSongInfo> list = DBSongInfo.descSortByLastPlayTime(all);
        int temp = activity.getResources().getInteger(R.integer.sheet_recent_count);
        int count = temp > all.size() ? all.size() : temp;
        data.bitmap = findBitmap(list, count);
        data.count = count;
        return data;
    }

    private Data getDataForAll(List<DBSongInfo> all) {
        Data data = new Data();
        data.bitmap = findBitmap(all, all.size());
        data.count = all.size();
        return data;
    }

    private void updateFavorite(Data data) {
        Bitmap b = data.bitmap;
        mImageFavorite.setImageBitmap(createImage(b));
        updateTextAndColor(mTextFavorite, data.count, mCountFavorite, data.colors);
    }

    private void updateRecent(Data data) {
        Bitmap b = data.bitmap;
        mImageRecent.setImageBitmap(createImage(b));
        updateTextAndColor(mTextRecent, data.count, mCountRecent, data.colors);
    }

    private void updateAll(Data data) {
        Bitmap b = data.bitmap;
        mImageAll.setImageBitmap(createImage(b));
        updateTextAndColor(mTextAll, data.count, mCountAll, data.colors);
    }

    private Bitmap findBitmap(List<DBSongInfo> list, int limit) {
        Bitmap bitmap = null;
        limit = limit > list.size() ? list.size() : limit;
        for (int i = 0; i < limit; i++) {
            DBSongInfo d = list.get(i);
            SongInfo info = mediaManager.getSongInfo(activity, d.path);
            bitmap = BitmapUtils.bitmapResizeFromFile(info.getAlbum_path(), mImageRecent.getWidth(), mImageRecent.getHeight());
            if (bitmap != null) {
                break;
            }
        }

        if (bitmap == null) {
            bitmap = BitmapUtils.getDefaultPictureForAlbum(activity, mImageRecent.getWidth(), mImageRecent.getHeight());
        }

        return bitmap;
    }

    private void updateTextAndColor(TextView categoryView, int count, TextView countView, int[] colors) {

        categoryView.setBackgroundColor(colors[2]);
        categoryView.setTextColor(colors[3]);

        GradientDrawable countD = new GradientDrawable();
        countD.setColor(colors[0]);
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
