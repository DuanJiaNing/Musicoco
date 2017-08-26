package com.duan.musicoco.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.duan.musicoco.R;
import com.duan.musicoco.modle.SongInfo;
import com.duan.musicoco.shared.DialogProvider;

/**
 * Created by DuanJiaNing on 2017/7/17.
 */

public class DialogUtils {


    public static void showDetailDialog(Activity activity, SongInfo info) {
        DialogProvider manager = new DialogProvider(activity);

        String[] infos = new String[7];
        infos[0] = "歌曲：" + info.getTitle();
        infos[1] = "歌手：" + info.getArtist();
        infos[2] = "专辑：" + info.getAlbum();
        infos[3] = "时长：" + StringUtils.getGenTimeMS((int) info.getDuration());
        infos[4] = "格式：" + info.getMime_type();
        infos[5] = "大小：" + String.valueOf(info.getSize() >> 10 >> 10) + " MB";
        infos[6] = "路径：" + info.getData();

        View view = activity.getLayoutInflater().inflate(R.layout.list_image, null);
        ListView listView = (ListView) view.findViewById(R.id.list_image_list);
        ImageView imageView = (ImageView) view.findViewById(R.id.list_image_image);
        listView.setAdapter(new ArrayAdapter<String>(
                activity,
                R.layout.text_view_start,
                infos
        ));

        Bitmap b = BitmapUtils.bitmapResizeFromFile(
                info.getAlbum_path(),
                imageView.getWidth(),
                imageView.getHeight());
        if (b == null) {
            b = BitmapUtils.getDefaultPictureForAlbum(activity, imageView.getWidth(), imageView.getHeight());
        }

        if (b != null) {
            imageView.setImageBitmap(b);
        }

        ColorDrawable drawable = new ColorDrawable(Color.WHITE);
        drawable.setAlpha(245);
        listView.setBackground(drawable);

        manager.createFullyCustomDialog(view, "歌曲信息").show();
    }


}
