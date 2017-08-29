package com.duan.musicoco.sheetmodify;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.RootActivity;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.db.modle.Sheet;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.view.TextInputHelper;

public class SheetModifyActivity extends RootActivity implements
        ThemeChangeable,
        View.OnClickListener {

    private FloatingActionButton done;
    private Toolbar toolbar;
    private TextView tip;
    private LinearLayout modifyContainer;
    private TextInputHelper.ViewHolder nameModifyHolder;
    private TextInputHelper.ViewHolder remarkModifyHolder;

    private String oldName;
    private String oldRemark;
    private boolean isNewSheet = false;

    private Sheet sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_modify);

        initViews();
        initSheet();
        initData();
        initToolBarTitle();
        themeChange(null, null);

    }

    private void initToolBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        String title = getString(R.string.modify_sheet);
        if (isNewSheet) {
            title = getString(R.string.new_sheet);
        }

        actionBar.setTitle(title);
    }

    private void initSheet() {
        Intent intent = getIntent();
        int si = intent.getIntExtra(ActivityManager.SHEET_MODIFY_ID, Integer.MAX_VALUE);

        if (Integer.MAX_VALUE != si) {
            if (si < 0) {
                sheet = null;
                isNewSheet = true;
            } else {
                sheet = dbController.getSheet(si);
                if (sheet == null) {
                    ToastUtils.showShortToast(getString(R.string.error_load_sheet_fail), this);
                    finish();
                } else {
                    oldName = sheet.name;
                    oldRemark = sheet.remark;
                    isNewSheet = false;
                }
            }
        } else {
            sheet = null;
            isNewSheet = true;
        }
    }

    private void initData() {

        int nameLimit = getResources().getInteger(R.integer.sheet_name_text_limit);
        int remarkLimit = getResources().getInteger(R.integer.sheet_remark_text_limit);
        int remarkInputMaxLines = getResources().getInteger(R.integer.sheet_remark_input_max_lines);

        String errorCountOutLimit = getString(R.string.error_text_count_out_of_limit);
        String hintName = getString(R.string.sheet_name);
        String hintRemark = getString(R.string.sheet_remark);

        String oldName = "";
        String oldRemark = "";

        if (!isNewSheet) {
            oldName = sheet.name;
            oldRemark = sheet.remark;
        }

        TextInputHelper helper = new TextInputHelper(this);

        nameModifyHolder = helper.createLimitedTexInputLayoutView(
                hintName,
                nameLimit,
                errorCountOutLimit,
                oldName
        );
        nameModifyHolder.editText.setLines(1);
        nameModifyHolder.editText.setSingleLine(true);

        remarkModifyHolder = helper.createLimitedTexInputLayoutView(
                hintRemark,
                remarkLimit,
                errorCountOutLimit,
                oldRemark
        );
        remarkModifyHolder.editText.setMaxLines(remarkInputMaxLines);

        modifyContainer.addView(nameModifyHolder.view, 0);
        modifyContainer.addView(remarkModifyHolder.view, 1);
    }

    private void initViews() {

        tip = (TextView) findViewById(R.id.sheet_modify_tip);
        modifyContainer = (LinearLayout) findViewById(R.id.sheet_modify_input_container);
        done = (FloatingActionButton) findViewById(R.id.sheet_modify_done);
        done.setOnClickListener(this);
        toolbar = (Toolbar) findViewById(R.id.sheet_modify_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {

        ThemeEnum th = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, th);

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

        updateFloatingBtColor(new int[]{accentC, toolbarMainTC, toolbarVicTC});

        tip.setTextColor(mainTC);
        nameModifyHolder.textViewLimit.setTextColor(vicTC);
        remarkModifyHolder.textViewLimit.setTextColor(vicTC);

        nameModifyHolder.editText.setHighlightColor(accentC);

        int[] ta = ColorUtils.get2ActionStatusBarColors(this);
        toolbar.setBackgroundColor(ta[1]);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ta[0]);
        }
    }

    private void updateFloatingBtColor(int[] colors) {

        int bgC = colors[0];
        int tintC = colors[1];
        int rippleC = colors[2];

        done.setRippleColor(rippleC);
        done.setBackgroundTintList(ColorStateList.valueOf(bgC));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            done.getDrawable().setTint(tintC);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.sheet_modify_done:
                if (isNewSheet) {
                    handleAddSheet();
                } else {
                    handleModifySheet();
                }
                break;
            default:
                break;
        }
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

    private void handleAddSheet() {
        String name = nameModifyHolder.editText.getText().toString();
        String error = null;

        if (TextUtils.isEmpty(name)) {
            error = getString(R.string.error_name_required);
        } else {
            String remark = remarkModifyHolder.editText.getText().toString();
            remark = TextUtils.isEmpty(remark) ? "" : remark;

            if (nameModifyHolder.textInputLayout.isErrorEnabled()) {
                TextInputHelper.textInputErrorTwinkle(nameModifyHolder.textInputLayout, "!");
                return;
            } else if (remarkModifyHolder.textInputLayout.isErrorEnabled()) {
                TextInputHelper.textInputErrorTwinkle(remarkModifyHolder.textInputLayout, "!");
                return;
            } else {
                String res = dbController.addSheet(name, remark, 0);
                if (res != null) {
                    error = res;
                }
            }
        }

        if (error != null) {
            nameModifyHolder.textInputLayout.setError(error);
            nameModifyHolder.textInputLayout.setErrorEnabled(true);
        } else {
            String msg = getString(R.string.success_create_sheet) + " [" + name + "]";
            ToastUtils.showShortToast(msg, this);
//            MainActivity 的 onResume 会更新【我的歌单】，因而不需要使用广播
//            broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_UPDATE, null);
            finish();
        }
    }

    private void handleModifySheet() {
        String newName = nameModifyHolder.editText.getText().toString();
        String newRemark = remarkModifyHolder.editText.getText().toString();

        if (newName.equals(oldName) && newRemark.equals(oldRemark)) {
            ToastUtils.showShortToast(getString(R.string.info_not_modify), this);
            finish();
            return;
        }

        String error = null;
        if (TextUtils.isEmpty(newName)) {
            error = getString(R.string.error_name_required);
        } else {
            newRemark = TextUtils.isEmpty(newRemark) ? "" : newRemark;

            if (nameModifyHolder.textInputLayout.isErrorEnabled()) {
                TextInputHelper.textInputErrorTwinkle(nameModifyHolder.textInputLayout, "!");
                return;
            } else if (remarkModifyHolder.textInputLayout.isErrorEnabled()) {
                TextInputHelper.textInputErrorTwinkle(remarkModifyHolder.textInputLayout, "!");
                return;
            } else {
                String res = dbController.updateSheet(sheet.id, newName, newRemark);
                if (res != null) {
                    error = res;
                }
            }
        }

        if (error != null) {
            nameModifyHolder.textInputLayout.setError(error);
            nameModifyHolder.textInputLayout.setErrorEnabled(true);
        } else {
            String msg = getString(R.string.success_modify_sheet) + " [" + newName + "]";
            ToastUtils.showShortToast(msg, this);
            //            MainActivity 的 onResume 会更新【我的歌单】，因而不需要使用广播
//            broadcastManager.sendMyBroadcast(BroadcastManager.FILTER_MY_SHEET_UPDATE, null);
            finish();
        }
    }
}
