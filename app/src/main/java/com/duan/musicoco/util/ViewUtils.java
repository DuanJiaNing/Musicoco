package com.duan.musicoco.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/8/4.
 */

public class ViewUtils {

    /**
     * @param colors length 为 4 的颜色值数组，0..4 分别对应 <br>
     *               收藏图标着色
     *               取消收藏图标着色
     *               收藏文字颜色
     *               取消收藏文字颜色
     *               中间线条颜色
     */
    public static View getSelectFavoriteOptionsView(Context context,
                                                    @Nullable View.OnClickListener favorite,
                                                    @Nullable View.OnClickListener cancelFavorite,
                                                    @NonNull int[] colors) {
        if (colors.length != 5) {
            return null;
        }

        int favoriteIC = colors[0];
        int notFavoriteIC = colors[1];
        int favoriteTC = colors[2];
        int notFavoriteTC = colors[3];
        int lineC = colors[4];

        View view = LayoutInflater.from(context).inflate(R.layout.select_favorite_options, null);

        ImageView fit = (ImageView) view.findViewById(R.id.select_favorite_true);
        ImageView fif = (ImageView) view.findViewById(R.id.select_favorite_false);
        TextView ftt = (TextView) view.findViewById(R.id.select_favorite_tv_true);
        TextView ftf = (TextView) view.findViewById(R.id.select_favorite_tv_false);
        View line = view.findViewById(R.id.select_favorite_line);

        line.setBackgroundColor(lineC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fit.getDrawable().setTint(favoriteIC);
            fif.getDrawable().setTint(notFavoriteIC);
        }

        ftt.setTextColor(favoriteTC);
        ftf.setTextColor(notFavoriteTC);

        if (favorite != null) {
            fit.setOnClickListener(favorite);
        }

        if (cancelFavorite != null) {
            fif.setOnClickListener(cancelFavorite);
        }

        return view;
    }

}
