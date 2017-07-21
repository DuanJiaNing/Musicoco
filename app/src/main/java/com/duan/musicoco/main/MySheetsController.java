package com.duan.musicoco.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.support.v4.widget.NestedScrollView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.aidl.IPlayControl;
import com.duan.musicoco.app.interfaces.OnContentUpdate;
import com.duan.musicoco.app.interfaces.OnEmptyMediaLibrary;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.interfaces.OnUpdateStatusChanged;
import com.duan.musicoco.app.manager.BroadcastManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.shared.DialogProvider;
import com.duan.musicoco.shared.MySheetsOperation;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.DialogUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.TextInputHelper;

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
    private MediaManager mediaManager;
    private MySheetsAdapter adapter;
    private List<Sheet> sheets;
    private IPlayControl control;
    private MySheetsOperation mySheetsOperation;

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

    }

    public void initData(IPlayControl control) {
        this.control = control;

        adapter = new MySheetsAdapter(activity, sheets, dbMusicoco, mediaManager, control);
        mListView.setAdapter(adapter);
        ((NestedScrollView) activity.findViewById(R.id.main_scroll)).smoothScrollTo(0, 0);

        mySheetsOperation = new MySheetsOperation(activity, control, dbMusicoco);

        hasInitData = true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.my_sheet_add:
                mySheetsOperation.handleAddSheet();
                break;
            case R.id.sheet_empty_add:
                mySheetsOperation.handleAddSheet();
                break;
        }
    }

    @Override
    public void themeChange(Theme theme, int[] colors) {
        int[] cs;
        switch (theme) {
            case DARK:
                cs = ColorUtils.get4DarkThemeColors(activity);
                break;
            case WHITE:
            default:
                cs = ColorUtils.get4WhiteThemeColors(activity);
                break;
        }
        int mainTC = cs[1];
        int vicTC = cs[3];
        int mainBC = cs[0];

        adapter.themeChange(theme, new int[]{mainTC, vicTC, mainBC});

        mTitle.setTextColor(mainTC);
        mTitleLine.setBackgroundColor(vicTC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAddSheet.getDrawable().setTint(vicTC);
        }

    }

    @Override
    public void emptyMediaLibrary() {
        View view = activity.getLayoutInflater().inflate(R.layout.sheets_empty, null);
        TextView add = (TextView) view.findViewById(R.id.sheet_empty_add);
        add.setOnClickListener(this);

        view.setTag(EMPTY_VIEW);
        mContainer.addView(view, 2);
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
            emptyMediaLibrary();
            mTitle.setText(title + "(0)");
        } else {
            View v = mContainer.getChildAt(2);
            if (v != null && ((int) v.getTag()) == EMPTY_VIEW) {
                mContainer.removeView(v);
            }
            mTitle.setText(title + "(" + sheets.size() + ")");
            adapter.notifyDataSetChanged();
        }
    }

    public boolean hasInitData() {
        return hasInitData;
    }

}
