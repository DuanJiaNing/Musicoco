package com.duan.musicoco.themecolor;

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
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.view.ColorPickerView;

public class ThemeColorCustomActivity extends RootActivity implements
        ColorPickerView.OnColorPickerChangeListener,
        View.OnClickListener {

    private Button modeStatus;
    private Button modeIcon;
    private ImageView picStatus;
    private ImageView picStatus_;
    private ImageView picIcon;
    private ImageView picIcon_;
    private ColorPickerView pickerPrimary;
    private ColorPickerView pickerDark;

    private int colorActionStatus = Integer.MAX_VALUE;
    private int colorIcon = Integer.MAX_VALUE;

    private boolean mode = false; // true 为图标颜色选择 false 为状态栏颜色选择

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_color_custom);

        initViews();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.theme_custom_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        findViews();

        pickerPrimary.setOnColorPickerChangeListener(this);
        pickerDark.setOnColorPickerChangeListener(this);

        modeStatus.setOnClickListener(this);
        modeIcon.setOnClickListener(this);

        pickerDark.post(new Runnable() {
            @Override
            public void run() {
                int co = appPreference.getStatusBarColor();
                pickerDark.setColors(Color.TRANSPARENT, co);
            }
        });

        mode = false;
        updateMode();
    }

    private void updateMode() {
        if (mode) {
            picIcon.setVisibility(View.VISIBLE);
            picIcon_.setVisibility(View.VISIBLE);
        } else {
            picIcon.setVisibility(View.INVISIBLE);
            picIcon_.setVisibility(View.INVISIBLE);
        }
    }


    private void findViews() {
        modeStatus = (Button) findViewById(R.id.theme_custom_mode_status);
        modeIcon = (Button) findViewById(R.id.theme_custom_mode_icon);
        picStatus = (ImageView) findViewById(R.id.theme_custom_pic_status);
        picStatus_ = (ImageView) findViewById(R.id.theme_custom_pic_status_);
        picIcon = (ImageView) findViewById(R.id.theme_custom_pic_icon);
        picIcon_ = (ImageView) findViewById(R.id.theme_custom_pic_icon_);
        pickerPrimary = (ColorPickerView) findViewById(R.id.theme_custom_color_primary);
        pickerDark = (ColorPickerView) findViewById(R.id.theme_custom_color_dark);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_theme_color_custom, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_theme_color_custom_save:
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
        int sizeC = getResources().getDimensionPixelSize(R.dimen.text_middle_l);
        int sizeD = getResources().getDimensionPixelSize(R.dimen.text_middle_s);
        if (v == modeStatus) {
            if (!mode) {
                return;
            }
            modeStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeC);
            modeIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeD);
            mode = false;
            updateMode();
        } else {
            if (mode) {
                return;
            }
            modeStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeD);
            modeIcon.setTextSize(TypedValue.COMPLEX_UNIT_PX, sizeC);
            mode = true;
            updateMode();
        }
    }
}
