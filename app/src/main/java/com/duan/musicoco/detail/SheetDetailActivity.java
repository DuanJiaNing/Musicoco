package com.duan.musicoco.detail;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

public class SheetDetailActivity extends AppCompatActivity {

    private SheetInfoController sheetInfoController;
    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private Sheet sheet;
    private boolean isMainSheet;
    private int sheetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_detail);

        Utils.transitionStatusBar(this);

        sheetInfoController = new SheetInfoController(this);
        dbController = new DBMusicocoController(this, true);
        mediaManager = MediaManager.getInstance(this);

        getSheet();

        initViews();

        initData();

    }

    private void getSheet() {
        Intent intent = getIntent();
        int si = intent.getIntExtra(ActivityManager.SHEET_DETAIL_ID, Integer.MAX_VALUE);

        if (Integer.MAX_VALUE != si) {
            if (si < 0) {
                sheetID = si;
                sheet = null;
                isMainSheet = true;
            } else {
                sheet = dbController.getSheet(si);
                if (sheet == null) {
                    ToastUtils.showShortToast(getString(R.string.error_load_sheet_fail));
                    finish();
                } else {
                    sheetID = si;
                    isMainSheet = false;
                }
            }
        }
    }

    private void initData() {

        sheetInfoController.initData(sheetID, sheet, dbController, mediaManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sheet_detail, menu);
        MenuItem heart = menu.findItem(R.id.sheet_detail_action_collection);
        MenuItem search = menu.findItem(R.id.sheet_detail_search);
        MenuItem edit = menu.findItem(R.id.sheet_detail_action_modify);

        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = getColor(R.color.white_d);
        } else {
            color = getResources().getColor(R.color.white_d);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            heart.getIcon().setTint(color);
            search.getIcon().setTint(color);
            edit.getIcon().setTint(color);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.sheet_detail_search:
                ToastUtils.showShortToast("search");
                break;
            case R.id.sheet_detail_action_collection:

                break;
            case R.id.sheet_detail_action_modify:

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {

        initSelfView();
        sheetInfoController.initView();

    }

    private void initSelfView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.sheet_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
