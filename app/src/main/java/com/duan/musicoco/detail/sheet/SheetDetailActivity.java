package com.duan.musicoco.detail.sheet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import com.duan.musicoco.R;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.app.manager.MediaManager;
import com.duan.musicoco.db.DBMusicocoController;
import com.duan.musicoco.db.Sheet;
import com.duan.musicoco.shared.ExceptionHandler;
import com.duan.musicoco.shared.SheetCoverHelper;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

public class SheetDetailActivity extends AppCompatActivity {

    private SheetInfoController infoController;
    private SheetSongListController songListController;

    private DBMusicocoController dbController;
    private MediaManager mediaManager;

    private Sheet sheet;
    private int sheetID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_detail);

        Utils.transitionStatusBar(this);

        infoController = new SheetInfoController(this);
        songListController = new SheetSongListController(this);

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
            } else {
                sheet = dbController.getSheet(si);
                if (sheet == null) {
                    ToastUtils.showShortToast(getString(R.string.error_load_sheet_fail));
                    finish();
                } else {
                    sheetID = si;
                }
            }
        }
    }

    private void initData() {

        infoController.initData(sheetID, sheet, dbController, mediaManager);

        //FIXME 启动 Activity 卡顿（阻塞）
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                songListController.initData(sheetID, dbController, mediaManager);
            }
        }, 300);
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
        infoController.initView();
        songListController.initViews();

    }

    private void initSelfView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.sheet_detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
