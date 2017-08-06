package com.duan.musicoco.search;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.OnThemeChange;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.db.MainSheetHelper;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;

/**
 * Created by DuanJiaNing on 2017/8/6.
 */

public class SearchActivity extends RootActivity implements OnThemeChange {

    private int mSheetId;
    private SearchController mSearchController;
    private Toolbar mToolbar;
    private View mResultContainer;
    private TextView mResult;
    private ImageButton mGoBack;
    private EditText mInput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        mSearchController = new SearchController();
        getSheet();
        initViews();

    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        initToolbarView();

        mResultContainer = findViewById(R.id.search_result_container);
        mResult = (TextView) findViewById(R.id.search_result);
        themeChange(null, null);

        mSearchController.initViews();

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

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mResultContainer.startAnimation(animation);

    }

    private void initToolbarView() {

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

        showResultContainer();
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
        ToastUtils.showShortToast("" + mSheetId);
    }


    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        ThemeEnum theme = appPreference.getTheme();
        int[] cs = new int[10];
        switch (theme) {
            case DARK: {
                cs = ColorUtils.get10DarkThemeColors(this);
                break;
            }
            case WHITE:
            default: {
                cs = ColorUtils.get10WhiteThemeColors(this);
                break;
            }
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

        mToolbar.setBackgroundColor(vicBC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mGoBack.getDrawable().setTint(toolbarC);
        }

        mResultContainer.setBackgroundColor(mainBC);
        mResult.setBackgroundColor(accentC);

    }
}
