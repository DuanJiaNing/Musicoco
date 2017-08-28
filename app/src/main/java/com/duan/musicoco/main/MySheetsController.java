package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.ContentUpdatable;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.SheetOperation;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsController implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        ThemeChangeable,
        ContentUpdatable {

    private static final int EMPTY_VIEW = 0x1;
    private static final int EMPTY_VIEW_INDEX = 0;
    private TextView mTitle;
    private TextView mAddSheet;
    private ListView mListView;
    private LinearLayout mEmptyListNoticeContainer;
    private View line;

    private Activity activity;
    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;
    private MySheetsAdapter adapter;
    private final List<Sheet> sheets;
    private IPlayControl control;
    private SheetOperation sheetOperation;

    private boolean hasInitData = false;

    public MySheetsController(Activity activity, DBMusicocoController dbMusicoco, MediaManager mediaManager) {
        this.activity = activity;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.sheets = new ArrayList<>();
    }

    public void initView() {
        mTitle = (TextView) activity.findViewById(R.id.my_sheet_title);
        mAddSheet = (TextView) activity.findViewById(R.id.my_sheet_add);
        mListView = (ListView) activity.findViewById(R.id.my_sheet_list);
        mEmptyListNoticeContainer = (LinearLayout) activity.findViewById(R.id.empty_list_notice_container);
        line = activity.findViewById(R.id.activity_main_line);

        mAddSheet.setOnClickListener(this);
        mListView.setOnItemClickListener(this);

    }

    public void initData(IPlayControl control) {
        this.control = control;

        sheetOperation = new SheetOperation(activity, control, dbMusicoco);
        adapter = new MySheetsAdapter(activity, sheets, dbMusicoco, mediaManager, control, sheetOperation);
        mListView.setAdapter(adapter);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.my_sheet_add:
            case R.id.sheet_empty_add:
                sheetOperation.addSheet();
                break;
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        if (themeEnum == null) {
            themeEnum = new AppPreference(activity).getTheme();
        }
        int[] cs = ColorUtils.get10ThemeColors(activity, themeEnum);

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

        if (isEmptyViewVisible()) {
            emptyViewThemeChange(new int[]{accentC});
        } else {
            adapter.themeChange(themeEnum, cs);
        }

        mTitle.setTextColor(mainTC);
        mAddSheet.setTextColor(mainTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (Drawable drawable : mTitle.getCompoundDrawables()) {
                if (drawable != null) {
                    drawable.setTint(accentC);
                }
            }
            for (Drawable d : mAddSheet.getCompoundDrawables()) {
                if (d != null) {
                    d.setTint(mainTC);
                }
            }
        }

        line.setBackgroundColor(vicTC);
    }

    public void setLineVisible(boolean visible) {
        line.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void emptyViewThemeChange(int[] colors) {

        int accentC;
        if (colors == null) {
            ThemeEnum themeEnum = new AppPreference(activity).getTheme();
            int[] cs = ColorUtils.get10ThemeColors(activity, themeEnum);

            accentC = cs[2];

        } else {
            accentC = colors[0];
        }

        View v = mEmptyListNoticeContainer.getChildAt(EMPTY_VIEW_INDEX);
        TextView text = (TextView) v.findViewById(R.id.sheet_empty_add);

        text.setTextColor(accentC);
        Drawable[] drawables = text.getCompoundDrawables();
        for (Drawable d : drawables) {
            if (d != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    d.setTint(accentC);
                }
            }
        }

    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {

        //注意不能修改 sheets 的引用，否则 notifyDataSetChanged 失效
        List<Sheet> newData = dbMusicoco.getSheets();
        sheets.clear();
        for (Sheet s : newData) {
            sheets.add(s);
        }

        String title = activity.getString(R.string.my_sheet_title);
        if (sheets.size() == 0) {
            adapter.notifyDataSetChanged();
            emptySheet();
            //需要检查主题
            emptyViewThemeChange(null);
            mTitle.setText(title + "(0)");
        } else {
            if (isEmptyViewVisible()) {
                mEmptyListNoticeContainer.removeViewAt(EMPTY_VIEW_INDEX);
                mEmptyListNoticeContainer.setVisibility(View.GONE);
                //需要检查主题
                themeChange(null, null);
            }
            mTitle.setText(title + "(" + sheets.size() + ")");
            adapter.notifyDataSetChanged();
        }

    }

    private void emptySheet() {
        if (!isEmptyViewVisible()) {
            View view = activity.getLayoutInflater().inflate(R.layout.sheets_empty, null);
            TextView add = (TextView) view.findViewById(R.id.sheet_empty_add);
            add.setOnClickListener(this);

            view.setTag(EMPTY_VIEW);
            mEmptyListNoticeContainer.setVisibility(View.VISIBLE);
            mEmptyListNoticeContainer.addView(view, EMPTY_VIEW_INDEX);
        }
    }

    @Override
    public void noData() {
    }

    private boolean isEmptyViewVisible() {
        View v = mEmptyListNoticeContainer.getChildAt(EMPTY_VIEW_INDEX);
        if (v != null && ((int) v.getTag()) == EMPTY_VIEW) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasInitData() {
        return hasInitData;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sheet sheet = (Sheet) adapter.getItem(position);
        ActivityManager.getInstance().startSheetDetailActivity(activity, sheet.id, null);
    }
}
