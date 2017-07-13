package com.duan.musicoco.main;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DuanJiaNing on 2017/7/13.
 */

public class MySheetsController implements
        View.OnClickListener,
        OnThemeChange,
        OnContentUpdate,
        OnEmptyMediaLibrary {

    private static final int EMPTY_VIEW = 0x1;
    private TextView mTitle;
    private ImageButton mAddSheet;
    private View mTitleLine;
    private ListView mListView;
    private LinearLayout mContainer;

    private Activity activity;
    private DBMusicocoController dbMusicoco;
    private MySheetsAdapter adapter;
    private List<DBMusicocoController.Sheet> sheets;

    private boolean hasInitData = false;

    public MySheetsController(Activity activity, DBMusicocoController dbMusicoco) {
        this.activity = activity;
        this.dbMusicoco = dbMusicoco;
        this.sheets = new ArrayList<>();
    }

    public void initView() {
        mTitle = (TextView) activity.findViewById(R.id.my_sheet_title);
        mAddSheet = (ImageButton) activity.findViewById(R.id.my_sheet_add);
        mTitleLine = activity.findViewById(R.id.my_sheet_line);
        mListView = (ListView) activity.findViewById(R.id.my_sheet_list);
        mContainer = (LinearLayout) activity.findViewById(R.id.my_sheet_container);

        mAddSheet.setOnClickListener(this);

    }

    public void initData() {

        adapter = new MySheetsAdapter(activity, sheets);
        mListView.setAdapter(adapter);
        ((NestedScrollView) activity.findViewById(R.id.main_scroll)).smoothScrollTo(0, 0);
        update(null);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.my_sheet_add:

                break;
        }
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        int[] cs = new int[2];
        if (colors == null) {
            switch (theme) {
                case DARK:
                    cs = ColorUtils.get2DarkThemeTextColor(activity);
                    break;
                case WHITE:
                default:
                    cs = ColorUtils.get2WhiteThemeTextColor(activity);
                    break;
            }
        } else if (colors.length >= 2) {
            cs = colors;
        }
        adapter.themeChange(theme, cs);

        int mainTC = cs[0];
        int vicTC = cs[1];
        mTitle.setTextColor(mainTC);
        mTitleLine.setBackgroundColor(vicTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAddSheet.getDrawable().setTint(vicTC);
        }

    }

    @Override
    public void emptyMediaLibrary() {
        View view = activity.getLayoutInflater().inflate(R.layout.sheets_empty, null);
        view.setTag(EMPTY_VIEW);
        mContainer.addView(view, 2);
    }

    @Override
    public void update(Object obj) {

        //注意不能修改 sheets 的引用，否则 notifyDataSetChanged 失效
        List<DBMusicocoController.Sheet> newData = dbMusicoco.getSheets();
        sheets.clear();
        for (DBMusicocoController.Sheet s : newData) {
            sheets.add(s);
        }

        if (sheets.size() == 0) {
            adapter.notifyDataSetChanged();
            emptyMediaLibrary();
        } else {
            View v = mContainer.getChildAt(2);
            Object obj1 = v.getTag();
            if (obj1 != null && ((int) obj1) == EMPTY_VIEW) {
                mContainer.removeView(v);
            }
            adapter.notifyDataSetChanged();
        }
    }


    public boolean hasInitData() {
        return hasInitData;
    }
}
