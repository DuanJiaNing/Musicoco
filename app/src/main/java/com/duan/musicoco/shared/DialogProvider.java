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
import com.duan.musicoco.preference.AppPreference;
import com.duan.musicoco.preference.ThemeEnum;
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
 * <p>
 * <p>
 * 一个 DialogProvider 只应该被用来产生一种类型的对话框，不应试图用同一对象同时产生不同类型的对话框
 */
public class DialogProvider {

    /**
     * 用于addView()的最外层容器
     */
    private LinearLayout mFirstOuter;

    private View mTopLine;

    private View mMiddleLine;

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

    private final int buttonTextSize;
    private final int buttonPadding;

    private ThemeEnum themeEnum;
    private int backgroundColor;
    private int mainTextColor;
    private int vicTextColor;
    private int topLineColor;
    private int middleLineColor;
    private int accentColor;

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
        mNeuterButton.setVisibility(View.GONE);

        mPositiveButton = (TextView) rootView.findViewById(R.id.dialog_positive);
        mPositiveButton.setVisibility(View.GONE);

        mNegativeButton = (TextView) rootView.findViewById(R.id.dialog_Negative);
        mNegativeButton.setVisibility(View.GONE);

        mTopLine = rootView.findViewById(R.id.dialog_line1);
        mMiddleLine = rootView.findViewById(R.id.dialog_line2);

        themeEnum = new AppPreference(context).getTheme();
        updateTheme();

    }

    public void setTheme(ThemeEnum themeEnum) {
        this.themeEnum = themeEnum;
        updateTheme();
    }

    private void updateTheme() {
        int[] colors = ColorUtils.get10ThemeColors(context, themeEnum);

        int statusC = colors[0];
        int toolbarC = colors[1];
        int accentC = colors[2];
        int mainBC = colors[3];
        int vicBC = colors[4];
        int mainTC = colors[5];
        int vicTC = colors[6];
        int navC = colors[7];
        int toolbarMainTC = colors[8];
        int toolbarVicTC = colors[9];

        backgroundColor = mainBC;
        mainTextColor = mainTC;
        vicTextColor = vicTC;
        topLineColor = accentC;
        accentColor = accentC;
        middleLineColor = vicTC;

        mTitle.setTextColor(mainTextColor);
        mTopLine.setBackgroundColor(topLineColor);
        mMiddleLine.setBackgroundColor(middleLineColor);
        mMessage.setTextColor(vicTextColor);
        mNeuterButton.setTextColor(accentColor);
        mPositiveButton.setTextColor(accentColor);
        mNegativeButton.setTextColor(accentColor);
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

        mNeuterButton.setVisibility(View.VISIBLE);
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

        mNegativeButton.setVisibility(View.VISIBLE);
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

        mPositiveButton.setVisibility(View.VISIBLE);
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
    public AlertDialog createInfosDialog(String titleInfo, String... messageInfo) {
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

    public Dialog createPromptDialog(String title, String info,
                                     @Nullable final DialogProvider.OnClickListener ensure,
                                     @Nullable final DialogProvider.OnClickListener cancel,
                                     boolean cancelable) {

        final Dialog dialog = getDialog();
        setOnPositiveButtonListener(context.getString(R.string.continue_), new DialogProvider.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (ensure != null) {
                    ensure.onClick(mPositiveButton);
                }
            }
        });

        if (cancelable) { // 能取消
            setOnNegativeButtonListener(context.getString(R.string.cancel), new DialogProvider.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (cancel != null) {
                        cancel.onClick(mNegativeButton);
                    }
                }
            });
        }

        dialog.setCancelable(cancelable);
        mTitle.setText(title);
        mMessage.setText(info);

        return dialog;
    }

    private AlertDialog getDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(rootView);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        return dialog;
    }

    public AlertDialog createCustomInsiderDialog(String title, @Nullable String message, View v) {
        mInsider.addView(v);
        mTitle.setText(title);
        if (message != null) {
            mMessage.setText(message);
        } else {
            mSecondOuter.removeView(mMessage);
        }
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
        progressBar.setLoadingColor(accentColor);

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

    public int getAccentColor() {
        return accentColor;
    }

    public int getTopLineColor() {
        return topLineColor;
    }

    public int getMiddleLineColor() {
        return middleLineColor;
    }
}
