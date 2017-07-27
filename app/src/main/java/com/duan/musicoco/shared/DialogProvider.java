package com.duan.musicoco.shared;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.App;
import com.duan.musicoco.preference.Theme;
import com.duan.musicoco.util.ColorUtils;
import com.victor.loading.rotate.RotateLoading;

/**
 * 对话框通用功能管理。传入上下文以初始出固定模板的对话框，
 * 固定模板包括：最外层框架、标题显示处、提示信息显示处、可为null的内层视图容器、三个按钮（中性，消极，积极）
 * 可替换方式包括：
 * （1）.最外层（完全自定义的对话框）、
 * （2）.自定义视图替换外层（保留最外层框架）、
 * （3）.自定义视图替换内层（保留最外层框架、标题显示处、提示信息显示处，三个按钮）
 * 注意：只有在调用了show..显示对话框之后才可以getDialog()获得实例。在自定义并保留三个固有按钮时可在外部调用对
 * 应按钮的setOn...ButtonListener(String title, final OnClickListener OnClickListener)并实现接口实现点击事件监听。
 */
public class DialogProvider {

    /**
     * 用于addView()的最外层容器
     */
    private LinearLayout mFirstOuter;

    private View mLine1;

    private View mLine2;

    /**
     * 用于addView()的外层容器(包括标题和三个Button)
     */
    private LinearLayout mSecondOuter;

    /**
     * 用于addView()的内层容器(不包括标题和三个Button)
     */
    private LinearLayout mInsider;

    /**
     * 用于addView()的外层容器，不包括按钮
     */
    private RelativeLayout mCustomContainer;

    /**
     * 显示标题处
     */
    private TextView mTitle;

    /**
     * 显示提示处
     */
    private TextView mMessage;

    /**
     * 中性按钮
     */
    private TextView mNeuterButton;

    /**
     * 消极按钮
     */
    private TextView mNegativeButton;

    /**
     * 中性按钮
     */
    private TextView mPositiveButton;

    /**
     * layout视图
     */
    private View rootView;

    /**
     * 上下文
     */
    private Context context;

    /**
     * Alerdialog构造者
     */
    private AlertDialog.Builder builder;

    /**
     * 对话框
     */
    private AlertDialog dialog;

    private final int buttonTextSize;
    private final int buttonPadding;

    private Theme theme;
    int backgroundColor;
    int mainTextColor;
    int vicTextColor;
    int lineAndButtonColor;

    public DialogProvider(Context context) {
        this.context = context;

        buttonTextSize = context.getResources().getDimensionPixelSize(R.dimen.text_dialog_button);
        buttonPadding = context.getResources().getDimensionPixelSize(R.dimen.dialog_padding);

        rootView = LayoutInflater.from(context).inflate(R.layout.dialog, null);
        mFirstOuter = (LinearLayout) rootView.findViewById(R.id.dialog_layout_outermost);
        mSecondOuter = (LinearLayout) rootView.findViewById(R.id.dialog_layout_outside);
        mInsider = (LinearLayout) rootView.findViewById(R.id.dialog_layout_inside);
        mCustomContainer = (RelativeLayout) rootView.findViewById(R.id.dialog_button_layout);
        mTitle = (TextView) rootView.findViewById(R.id.dialog_title);
        mMessage = (TextView) rootView.findViewById(R.id.dialog_message);

        ViewGroup.LayoutParams p = mCustomContainer.getLayoutParams();
        p.height = 0;
        mCustomContainer.setLayoutParams(p);

        mNeuterButton = (TextView) rootView.findViewById(R.id.dialog_Neuter);
        mNeuterButton.setEnabled(false);

        mPositiveButton = (TextView) rootView.findViewById(R.id.dialog_positive);
        mPositiveButton.setEnabled(false);

        mNegativeButton = (TextView) rootView.findViewById(R.id.dialog_Negative);
        mNegativeButton.setEnabled(false);

        mLine1 = rootView.findViewById(R.id.dialog_line1);
        mLine2 = rootView.findViewById(R.id.dialog_line2);

        builder = new AlertDialog.Builder(context);

        theme = App.getAppPreference().getTheme();
        updateTheme();

    }

    public void setTheme(Theme theme) {
        this.theme = theme;
        updateTheme();
    }

    private void updateTheme() {
        int[] colors;
        switch (theme) {
            case DARK:
                colors = ColorUtils.get4DarkDialogThemeColors();
                break;
            case WHITE:
            default:
                colors = ColorUtils.get4WhiteDialogThemeColors();
                break;
        }

        backgroundColor = colors[0];
        mainTextColor = colors[1];
        vicTextColor = colors[2];
        lineAndButtonColor = colors[3];

        mTitle.setTextColor(mainTextColor);
        mLine1.setBackgroundColor(lineAndButtonColor);
        mLine2.setBackgroundColor(lineAndButtonColor);
        mMessage.setTextColor(vicTextColor);
        mNeuterButton.setTextColor(lineAndButtonColor);
        mPositiveButton.setTextColor(lineAndButtonColor);
        mNegativeButton.setTextColor(lineAndButtonColor);
        mFirstOuter.setBackgroundColor(backgroundColor);
    }

    /**
     * 外部实现按钮点击事件方法体
     */
    public interface OnClickListener {
        void onClick(View view);
    }

    /**
     * 中性按钮点击事件
     *
     * @param text            按钮填充文字
     * @param OnClickListener 外部实现点击事件
     */
    public void setOnNeuterButtonListener(String text, final OnClickListener OnClickListener) {
        ViewGroup.LayoutParams p = mCustomContainer.getLayoutParams();
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mCustomContainer.setLayoutParams(p);

        mNeuterButton.setEnabled(true);
        mNeuterButton.setTextSize(buttonTextSize);
        mNeuterButton.setPadding(buttonPadding + 1, buttonPadding, buttonPadding + 1, buttonPadding);
        mNeuterButton.setText(text);
        mNeuterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickListener.onClick(mNeuterButton);
            }
        });
    }

    /**
     * 消极按钮点击事件
     *
     * @param text            按钮填充文字
     * @param OnClickListener 外部实现点击事件
     */
    public void setOnNegativeButtonListener(String text, final OnClickListener OnClickListener) {
        ViewGroup.LayoutParams p = mCustomContainer.getLayoutParams();
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mCustomContainer.setLayoutParams(p);

        mNegativeButton.setEnabled(true);
        mNegativeButton.setTextSize(buttonTextSize);
        mNegativeButton.setPadding(buttonPadding + 1, buttonPadding, buttonPadding + 1, buttonPadding);
        mNegativeButton.setText(text);
        mNegativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickListener.onClick(mNegativeButton);
            }
        });
    }

    /**
     * 积极按钮点击事件
     *
     * @param text            按钮填充文字
     * @param OnClickListener 外部实现点击事件
     */
    public void setOnPositiveButtonListener(String text, final OnClickListener OnClickListener) {
        ViewGroup.LayoutParams p = mCustomContainer.getLayoutParams();
        p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mCustomContainer.setLayoutParams(p);

        mPositiveButton.setEnabled(true);
        mPositiveButton.setTextSize(buttonTextSize);
        mPositiveButton.setPadding(buttonPadding + 1, buttonPadding, buttonPadding + 1, buttonPadding);
        mPositiveButton.setText(text);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickListener.onClick(mPositiveButton);
            }
        });
    }

    /**
     * 显示信息提示对话框
     *
     * @param messageInfo 要显示的提示信息
     * @param titleInfo   标题
     */
    public AlertDialog createInfosDialog(String titleInfo, String[] messageInfo) {
        if (messageInfo.length == 1) {
            mMessage.setText(messageInfo[0]);
        } else {
            mSecondOuter.removeView(mMessage);
            for (int i = 0; i < messageInfo.length; i++) {
                TextView text = new TextView(context);
                text.setText(messageInfo[i]);
                text.setPadding(16, 8, 0, 0);
                mInsider.addView(text);
            }
        }
        mTitle.setText(titleInfo);

        return getDialog();

    }

    public AlertDialog createPromptDialog(String title, String info) {
        mTitle.setText(title);
        mMessage.setText(info);
        return getDialog();
    }

    private AlertDialog getDialog() {

        builder.setView(rootView);
        dialog = builder.create();

        return builder.create();
    }

    public AlertDialog createCustomInsiderDialog(String title, @Nullable String message, View v) {
        mInsider.addView(v);
        mTitle.setText(title);
        if (message != null) {
            mMessage.setText(message);
        } else mFirstOuter.removeView(mMessage);
        return getDialog();

    }

    /**
     * 显示外层视图填充（如（单选）列表对话框）
     *
     * @param v 填充的视图
     */
    public AlertDialog createCustomSecondOuterDialog(View v) {
        mSecondOuter.removeAllViews();
        mSecondOuter.addView(v);
        return getDialog();

    }

    /**
     * 显示完全自定义视图的对话框
     *
     * @param v 填充的视图
     */
    public AlertDialog createFullyCustomDialog(View v) {
        mFirstOuter.removeAllViews();
        mFirstOuter.addView(v);
        return getDialog();

    }

    /**
     * 显示保留标题显示外层视图填充
     *
     * @param v 填充的视图
     */
    public AlertDialog createFullyCustomDialog(View v, String titleInfo) {
        mTitle.setText(titleInfo);
        mSecondOuter.removeView(mMessage);
        mSecondOuter.removeView(mInsider);
        mSecondOuter.removeView(mCustomContainer);
        mSecondOuter.addView(v);

        return getDialog();
    }


    public Dialog createProgressDialog(String title) {
        View view = LayoutInflater.from(context).inflate(R.layout.progress, null);
        TextView titleT = (TextView) view.findViewById(R.id.progress_title);
        titleT.setText(title);
        titleT.setTextColor(mainTextColor);

        final RotateLoading progressBar = (RotateLoading) view.findViewById(R.id.progress_rotate_loading);
        progressBar.setLoadingColor(lineAndButtonColor);

        createCustomSecondOuterDialog(view);
        Dialog dialog = getDialog();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                progressBar.start();
            }
        });
        return dialog;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getMainTextColor() {
        return mainTextColor;
    }

    public int getVicTextColor() {
        return vicTextColor;
    }

    public int getLineAndButtonColor() {
        return lineAndButtonColor;
    }
}
