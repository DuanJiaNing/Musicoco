package com.duan.musicoco.main;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.App;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.bean.Sheet;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.shared.SheetsOperation;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsController implements
        View.OnClickListener,
        AdapterView.OnItemClickListener,
        OnThemeChange,
        OnContentUpdate,
        OnEmptyMediaLibrary {

    private static final int EMPTY_VIEW = 0x1;
    private static final int EMPTY_VIEW_INDEX = 2;
    private TextView mTitle;
    private ImageButton mAddSheet;
    private View mTitleLine;
    private ListView mListView;
    private LinearLayout mContainer;

    private Activity activity;
    private DBMusicocoController dbMusicoco;
    private MediaManager mediaManager;
    private MySheetsAdapter adapter;
    private final List<Sheet> sheets;
    private IPlayControl control;
    private SheetsOperation sheetsOperation;

    private boolean hasInitData = false;

    public MySheetsController(Activity activity, DBMusicocoController dbMusicoco, MediaManager mediaManager) {
        this.activity = activity;
        this.dbMusicoco = dbMusicoco;
        this.mediaManager = mediaManager;
        this.sheets = new ArrayList<>();
    }

    public void initView() {
        mTitle = (TextView) activity.findViewById(R.id.my_sheet_title);
        mAddSheet = (ImageButton) activity.findViewById(R.id.my_sheet_add);
        mTitleLine = activity.findViewById(R.id.my_sheet_line);
        mListView = (ListView) activity.findViewById(R.id.my_sheet_list);
        mContainer = (LinearLayout) activity.findViewById(R.id.my_sheet_container);

        mAddSheet.setOnClickListener(this);
        mListView.setOnItemClickListener(this);

    }

    public void initData(IPlayControl control) {
        this.control = control;

        sheetsOperation = new SheetsOperation(activity, control, dbMusicoco);

        adapter = new MySheetsAdapter(activity, sheets, dbMusicoco, mediaManager, control, sheetsOperation);
        mListView.setAdapter(adapter);
        ((NestedScrollView) activity.findViewById(R.id.main_scroll)).smoothScrollTo(0, 0);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.my_sheet_add:
            case R.id.sheet_empty_add:
                sheetsOperation.handleAddSheet();
                break;
        }
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        if (theme == null) {
            theme = ((App) activity.getApplicationContext()).getAppPreference().getTheme();
        }
        int[] cs;
        switch (theme) {
            case DARK:
                cs = ColorUtils.get10DarkThemeColors(activity);
                break;
            case WHITE:
            default:
                cs = ColorUtils.get10WhiteThemeColors(activity);
                break;
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

        if (isEmptyViewVisible()) {
            emptyViewThemeChange(new int[]{accentC});
        } else {
            adapter.themeChange(theme, cs);
        }

        mTitle.setTextColor(mainTC);
        mTitleLine.setBackgroundColor(accentC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAddSheet.getDrawable().setTint(mainTC);
        }

    }

    private void emptyViewThemeChange(int[] colors) {

        int accentC;
        if (colors == null) {
            Theme theme = ((App) activity.getApplicationContext()).getAppPreference().getTheme();
            int[] cs;
            switch (theme) {
                case DARK:
                    cs = ColorUtils.get10DarkThemeColors(activity);
                    break;
                case WHITE:
                default:
                    cs = ColorUtils.get10WhiteThemeColors(activity);
                    break;
            }

            accentC = cs[2];

        } else {
            accentC = colors[0];
        }

        View v = mContainer.getChildAt(EMPTY_VIEW_INDEX);
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
    public void emptyMediaLibrary() {
        if (!isEmptyViewVisible()) {
            View view = activity.getLayoutInflater().inflate(R.layout.sheets_empty, null);
            TextView add = (TextView) view.findViewById(R.id.sheet_empty_add);
            add.setOnClickListener(this);

            view.setTag(EMPTY_VIEW);
            mContainer.addView(view, EMPTY_VIEW_INDEX);
        }
    }

    @Override
    public void update(Object obj, OnUpdateStatusChanged completed) {
        Log.d("update", "MySheetController update");

        //注意不能修改 sheets 的引用，否则 notifyDataSetChanged 失效
        List<Sheet> newData = dbMusicoco.getSheets();
        sheets.clear();
        for (Sheet s : newData) {
            sheets.add(s);
        }

        String title = activity.getString(R.string.my_sheet_title);
        if (sheets.size() == 0) {
            adapter.notifyDataSetChanged();
            emptyMediaLibrary();
            //需要检查主题
            emptyViewThemeChange(null);
            mTitle.setText(title + "(0)");
        } else {
            if (isEmptyViewVisible()) {
                mContainer.removeViewAt(EMPTY_VIEW_INDEX);
                //需要检查主题
                themeChange(null, null);
            }
            mTitle.setText(title + "(" + sheets.size() + ")");
            adapter.notifyDataSetChanged();
        }

    }

    private boolean isEmptyViewVisible() {
        View v = mContainer.getChildAt(EMPTY_VIEW_INDEX);
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
        ActivityManager.getInstance(activity).startSheetDetailActivity(sheet.id);
    }
}
