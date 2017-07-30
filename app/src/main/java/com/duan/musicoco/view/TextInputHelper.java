package com.duan.musicoco.view;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.duan.musicoco.R;

/**
 * texInputLayoutManager管理
 */
public class TextInputHelper {

    private Context context;

    public TextInputHelper(Context context) {
        this.context = context;

    }

    /**
     * 获得有字数限制的输入框，字数超限会有超限提示
     *
     * @param hint      hint
     * @param limit     输入字数上限
     * @param errorInfo 超限提示
     * @return view
     */
    public ViewHolder createLimitedTexInputLayoutView(String hint, final int limit, final String errorInfo, String text) {

        final ViewHolder holder = new ViewHolder(context);

        holder.textInputLayout.setHint(hint);
        if (text != null) {
            holder.editText.setText(text);
        }

        holder.textViewLimit.setText(holder.editText.getText().length() + "/" + limit);
        holder.editText.addTextChangedListener(new TextWatcher() {

            /**
             * 输入框内容改变之前
             * @param s 改变之前的字符串
             * @param start 改变的起始位置
             * @param count 改变的个数
             * @param after 都为0
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                holder.textInputLayout.setErrorEnabled(false);
            }

            /**
             * 改变的过程中
             * @param s 输入框每一次变化后的字符串
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                holder.textViewLimit.setText(s.length() + "/" + limit);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > limit) {
                    String str = errorInfo;
                    holder.textInputLayout.setError(str);
                    holder.textInputLayout.setErrorEnabled(true);
                } else {
                    holder.textInputLayout.setErrorEnabled(false);
                }
            }
        });
        return holder;
    }

    public ViewHolder createGeneralTexInputLayoutView(String hint, String text) {
        final ViewHolder holder = new ViewHolder(context);

        holder.textInputLayout.setHint(hint);
        if (text != null) {
            holder.editText.setText(text);
        }
        return holder;
    }

    //错误信息闪烁
    public static void textInputErrorTwinkle(TextInputLayout il, String str) {
        String error = (String) il.getError();
        if (TextUtils.isEmpty(error)) {
            error = TextUtils.isEmpty(str) ? "!" : str;
        }

        il.setErrorEnabled(false);
        il.setError(error);
        il.setErrorEnabled(true);
    }

    public class ViewHolder {
        public View view;
        public TextInputLayout textInputLayout;
        public EditText editText;
        public TextView textViewLimit;

        public ViewHolder(Context context) {
            this.view = View.inflate(context, R.layout.text_input, null);
            this.textInputLayout = (TextInputLayout) view.findViewById(R.id.TextInputLayout);
            this.editText = textInputLayout.getEditText();
            this.textViewLimit = (TextView) view.findViewById(R.id.text_input_text);
        }
    }

}
