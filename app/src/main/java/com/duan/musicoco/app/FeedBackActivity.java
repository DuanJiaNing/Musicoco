package com.duan.musicoco.app;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.PermissionManager;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.shared.DialogProvider;
import com.duan.musicoco.util.ColorUtils;
import com.duan.musicoco.util.StringUtils;
import com.duan.musicoco.util.ToastUtils;
import com.duan.musicoco.util.Utils;

import java.util.ArrayList;

public class FeedBackActivity extends RootActivity implements
        ThemeChangeable,
        View.OnClickListener {

    private Button send;
    private EditText input;
    private Toolbar toolbar;

    private final int SMS_REQUEST_CODE = 0x1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);

        checkPermission();

        initViews();
        themeChange(null, null);
    }

    private void checkPermission() {
        PermissionManager pm = PermissionManager.getInstance();
        final String re = "android.permission.SEND_SMS";
        if (!pm.checkPermission(this, re)) {
            pm.requestPermissions(this, new String[]{re}, SMS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                finish();
            }
        }

    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.feedback_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        send = (Button) findViewById(R.id.feedback_send);
        input = (EditText) findViewById(R.id.feedback_input);
        send.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void themeChange(ThemeEnum themeEnum, int[] colors) {
        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);
        int accentC = cs[2];
        int mainTC = cs[5];
        int vicTC = cs[6];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            send.setBackgroundTintList(ColorStateList.valueOf(accentC));
        }

        input.setHintTextColor(vicTC);
        input.setTextColor(mainTC);
        initInputBg(accentC, theme == ThemeEnum.WHITE ? Color.WHITE : cs[4]);

        TextView cent = (TextView) findViewById(R.id.feedback_tip_msg);
        TextView other = (TextView) findViewById(R.id.feedback_tip_other_where);
        TextView tip = (TextView) findViewById(R.id.feedback_tip);
        View tipLine = findViewById(R.id.feedback_tip_line);
        TextView tipO = (TextView) findViewById(R.id.feedback_tip_ow);
        View tipOL = findViewById(R.id.feedback_tip_ow_line);
        cent.setTextColor(vicTC);
        other.setTextColor(vicTC);
        other.setLinkTextColor(accentC);

        tipLine.setBackgroundColor(vicTC);
        tip.setTextColor(mainTC);

        tipOL.setBackgroundColor(vicTC);
        tipO.setTextColor(mainTC);

        int[] cs2 = ColorUtils.get2ActionStatusBarColors(this);
        int actionC = cs2[0];
        int statusC = cs2[1];
        toolbar.setBackgroundColor(statusC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(actionC);
        }

    }

    private void initInputBg(int accentC, int vicBC) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(vicBC);
        gd.setCornerRadius(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gd.setStroke(3, new ColorStateList(
                    new int[][]{
                            {android.R.attr.state_focused},
                            {}},
                    new int[]{
                            accentC,
                            android.support.v4.graphics.ColorUtils.setAlphaComponent(accentC, 200)
                    }
            ));
        } else {
            gd.setStroke(3, accentC);
        }

        input.setBackground(gd);

    }

    @Override
    public void onClick(View v) {
        String str = input.getText().toString();
        if (StringUtils.isReal(str)) {
            if (str.length() > 300) {
                String msg = getString(R.string.error_too_many_words_for_300);
                ToastUtils.showShortToast(msg, this);
            } else {
                handleFeedback(str);
            }
        } else {
            String msg = getString(R.string.info_feedback_input_empty);
            ToastUtils.showShortToast(msg, this);
        }
    }

    private void handleFeedback(final String str) {

        DialogProvider p = new DialogProvider(this);
        final Dialog progressDialog = p.createProgressDialog(getString(R.string.in_progress_send_feedback));

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                progressDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {

                String phone = getString(R.string.phone);
                SmsManager manager = SmsManager.getDefault();
                //因为一条短信有字数限制，因此要将长短信拆分
                ArrayList<String> list = manager.divideMessage(str);
                for (String text : list) {
                    manager.sendTextMessage(phone, null, text, null, null);
                }

                Utils.pretendToRun(3000);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.hide();
                String msg = getString(R.string.success_feedback);
                ToastUtils.showLongToast(FeedBackActivity.this, msg);
            }
        }.execute();

    }
}
