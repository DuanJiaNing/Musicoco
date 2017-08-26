package com.duan.musicoco.search;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.db.modle.DBSongInfo;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/8/6.
 */

public class SearchActivity extends RootActivity implements ThemeChangeable {

    private int mSheetId;
    private SearchController mSearchController;
    private MainSheetHelper mainSheetHelper;
    private List<DBSongInfo> infos;

    private Toolbar mToolbar;
    private View mResultContainer;
    private EditText mInput;

    private boolean isResultContainerShowing = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        mainSheetHelper = new MainSheetHelper(this, dbController);
        getSheet();
        mSearchController = new SearchController(this, dbController, mSheetId, infos);

        initViews();
        initData();

    }

    private void initData() {
        mSearchController.initData();
        initInputListener();
        themeChange(null, null);
    }

    private void initInputListener() {
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSearchController.update(s + "");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mInput = (EditText) findViewById(R.id.search_input);
        mResultContainer = findViewById(R.id.search_result_container);

        mSearchController.initViews();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showResultContainer() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);
        int duration = getResources().getInteger(R.integer.anim_default_duration_half);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mResultContainer.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isResultContainerShowing = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mResultContainer.startAnimation(animation);

    }

    @Override
    public void finish() {
        hideResultContainerThenFinish();
    }

    private void hideResultContainerThenFinish() {

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        int duration = getResources().getInteger(R.integer.anim_default_duration_half);
        animation.setDuration(duration);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mResultContainer.setVisibility(View.GONE);
                SearchActivity.super.finish();
                overridePendingTransition(
                        R.anim.slide_in_right_no_alpha,
                        R.anim.slide_out_right_no_alpha);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mResultContainer.startAnimation(animation);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();

        if (!isResultContainerShowing) {
            showResultContainer();
        }
    }

    @Override
    protected void checkTheme() {
        ThemeEnum themeEnum = appPreference.getTheme();
        if (themeEnum == ThemeEnum.DARK) {
            this.setTheme(R.style.Theme_Search_DARK);
        } else {
            this.setTheme(R.style.Theme_Search_WHITE);
        }
    }

    private void getSheet() {
        Intent intent = getIntent();
        int si = intent.getIntExtra(ActivityManager.SHEET_SEARCH_ID, Integer.MAX_VALUE);

        if (Integer.MAX_VALUE != si) {
            mSheetId = si;
        } else {
            mSheetId = MainSheetHelper.SHEET_ALL;
        }

        validate();
    }

    private void validate() {
        if (mSheetId < 0) {
            infos = mainSheetHelper.getMainSheetSongInfo(mSheetId);
        } else {
            infos = dbController.getSongInfos(mSheetId);
        }

        if (infos == null || infos.size() == 0) {
            String msg = getString(R.string.error_empty_sheet);
            ToastUtils.showShortToast(msg, this);
            finish();
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);
        mSearchController.themeChange(theme, cs);

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

        mResultContainer.setBackgroundColor(mainBC);
        mInput.setHintTextColor(vicTC);
        mInput.setTextColor(mainTC);

        Drawable icon = getResources().getDrawable(R.drawable.ic_arrow_back);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon.setTint(accentC);
        }
        mToolbar.setNavigationIcon(icon);

        int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }
    }
}
