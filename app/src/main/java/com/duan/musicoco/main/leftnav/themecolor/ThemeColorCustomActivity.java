package com.duan.musicoco.main.leftnav.themecolor;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.ColorPickerView;

public class ThemeColorCustomActivity extends RootActivity implements
        ColorPickerView.OnColorPickerChangeListener,
        View.OnClickListener,
        ThemeChangeable {

    private Button modeStatus;
    private Button modeIcon;
    private ImageView picStatus_;
    private ImageView picIcon;
    private ImageView picIcon_;
    private ColorPickerView pickerPrimary;
    private ColorPickerView pickerDark;

    private Toolbar toolbar;

    private int colorActionStatus = Integer.MAX_VALUE;
    private int colorIcon = Integer.MAX_VALUE;

    private boolean mode = false; // true 为图标颜色选择 false 为状态栏颜色选择

    private int mainTC, vicTC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_color_custom);

        initViews();
        themeChange(null, null);
        mode = false;
        updateMode();
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.theme_custom_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        findViews();

        pickerPrimary.setOnColorPickerChangeListener(this);
        pickerDark.setOnColorPickerChangeListener(this);

        modeStatus.setOnClickListener(this);
        modeIcon.setOnClickListener(this);

    }

    private void updateMode() {
        int sizeC = getResources().getDimensionPixelSize(R.dimen.text_middle_l);
        int sizeD = getResources().getDimensionPixelSize(R.dimen.text_middle_s);
        if (mode) {

            modeStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeD);
            modeIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeC);
            modeStatus.setTextColor(vicTC);
            modeIcon.setTextColor(mainTC);

            picIcon.setVisibility(View.VISIBLE);
            picIcon_.setVisibility(View.VISIBLE);
        } else {
            modeStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeC);
            modeIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeD);
            modeStatus.setTextColor(mainTC);
            modeIcon.setTextColor(vicTC);

            picIcon.setVisibility(View.INVISIBLE);
            picIcon_.setVisibility(View.INVISIBLE);
        }
    }


    private void findViews() {
        modeStatus = (Button) findViewById(R.id.theme_custom_mode_status);
        modeIcon = (Button) findViewById(R.id.theme_custom_mode_icon);
        picStatus_ = (ImageView) findViewById(R.id.theme_custom_pic_status_);
        picIcon = (ImageView) findViewById(R.id.theme_custom_pic_icon);
        picIcon_ = (ImageView) findViewById(R.id.theme_custom_pic_icon_);
        pickerPrimary = (ColorPickerView) findViewById(R.id.theme_custom_color_primary);
        pickerDark = (ColorPickerView) findViewById(R.id.theme_custom_color_dark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                handleSave();
                finish();
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleSave() {
        if (colorActionStatus != Integer.MAX_VALUE) {
            appPreference.updateStatusBarColor(colorActionStatus);
            appPreference.updateActionbarColor(colorActionStatus);
            // 颜色自定义只在日间主题时有效
            appPreference.updateTheme(ThemeEnum.WHITE);
        }

        if (colorIcon != Integer.MAX_VALUE) {
            appPreference.updateAccentColor(colorIcon);
            appPreference.updateTheme(ThemeEnum.WHITE);
        }
    }

    @Override
    public void onColorChanged(ColorPickerView picker, int color) {
        if (picker == pickerPrimary) {
            pickerDark.setColors(Color.TRANSPARENT, color);
        }

        if (mode) {
            colorIcon = pickerDark.getColor();
        } else {
            colorActionStatus = pickerDark.getColor();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            updateColor();
        } else {
            String msg = getString(R.string.error_action_did_not_support);
            ToastUtils.showShortToast(msg, this);
            finish();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateColor() {
        if (mode) {
            picIcon_.getDrawable().setTint(colorIcon);
        } else {
            picStatus_.getDrawable().setTint(colorActionStatus);
        }
    }

    @Override
    public void onStartTrackingTouch(ColorPickerView picker) {

    }

    @Override
    public void onStopTrackingTouch(ColorPickerView picker) {

    }

    @Override
    public void onClick(View v) {
        if (v == modeStatus) {
            if (!mode) {
                return;
            }
            mode = false;
            updateMode();
        } else {
            if (mode) {
                return;
            }
            mode = true;
            updateMode();
        }
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        final int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }

        pickerDark.post(new Runnable() {
            @Override
            public void run() {
                pickerDark.setColors(Color.TRANSPARENT, ta[1]);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            picStatus_.getDrawable().setTint(ta[1]);
            int ac = ColorUtils.getAccentColor(this);
            picIcon_.getDrawable().setTint(ac);
        }

        int[] cs = ColorUtils.get2ThemeTextColor(this, appPreference.getTheme());
        mainTC = cs[0];
        vicTC = cs[1];

    }
}
