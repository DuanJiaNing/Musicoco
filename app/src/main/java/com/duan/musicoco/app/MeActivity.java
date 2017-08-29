package com.duan.musicoco.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.duan.musicoco.R;
import com.duan.musicoco.app.interfaces.ThemeChangeable;
import com.duan.musicoco.app.manager.ActivityManager;
import com.duan.musicoco.preference.ThemeEnum;
import com.duan.musicoco.util.BitmapUtils;
import com.duan.musicoco.util.ColorUtils;

public class MeActivity extends RootActivity implements ThemeChangeable {

    private Toolbar toolbar;
    private TextView social;
    private RecyclerView recyclerView;
    private TextView openSource;
    private TextView sourceUrl;

    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        activityManager = ActivityManager.getInstance();
        initViews();
        themeChange(null, null);
        initData();
    }

    private void initData() {

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        SocialAdapter adapter = new SocialAdapter(this);
        adapter.setOnItemClickListener(new SocialAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, String url, String text) {
                handleItemClick(text, url, position);
            }
        });
        recyclerView.setAdapter(adapter);

    }

    private void handleItemClick(String text, String url, int position) {
        switch (position) {
            case 0:  // github
            case 1: // csdn
            case 2: // jianshu
                activityManager.startWebActivity(this, url);
                break;
            case 3: // qq
                activityManager.startQQChartPanel(this, url);
                break;
            case 4: // email
                activityManager.startSystemEmailPanel(this, url);
                break;
            case 5: // me
                activityManager.startSystemPhoneCallPanel(this, url);
                break;
        }
    }

    private void initViews() {

        toolbar = (Toolbar) findViewById(R.id.me_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.me_recycle_view);
        social = (TextView) findViewById(R.id.me_social);
        openSource = (TextView) findViewById(R.id.me_open_source);
        sourceUrl = (TextView) findViewById(R.id.me_open_source_url);
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

        int[] cs2 = ColorUtils.get2ActionStatusBarColors(this);
        int actionC = cs2[0];
        int statusC = cs2[1];
        toolbar.setBackgroundColor(statusC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(actionC);
        }

        ThemeEnum theme = appPreference.getTheme();
        int[] cs = ColorUtils.get10ThemeColors(this, theme);
        int mainTC = cs[5];
        int vicTC = cs[6];
        int accent = cs[2];

        social.setTextColor(mainTC);
        openSource.setTextColor(mainTC);
        sourceUrl.setTextColor(vicTC);
        sourceUrl.setLinkTextColor(accent);

    }

    private static class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {

        private final Context context;
        private final String[] texts;
        private final String[] url;
        private final int[] images = {
                R.drawable.me_github,
                R.drawable.me_csdn,
                R.drawable.me_jianshu,
                R.drawable.me_qq,
                R.drawable.me_email,
                R.drawable.me_me
        };

        public SocialAdapter(Context context) {
            this.context = context;

            texts = context.getResources().getStringArray(R.array.about_me_social);
            url = context.getResources().getStringArray(R.array.about_me_social_url);
        }

        private OnItemClickListener listener;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.listener = listener;
        }

        interface OnItemClickListener {
            void onItemClick(int position, String url, String text);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.me_socail_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String text = texts[position];
            Bitmap b = BitmapUtils.bitmapResizeFromResource(
                    context.getResources(),
                    images[position],
                    holder.image.getMeasuredWidth(), holder.image.getMeasuredHeight());

            int dTC = Color.WHITE;
            int dBC = ColorUtils.getAccentColor(context);
            int[] colors = new int[4];
            ColorUtils.get4LightColorWithTextFormBitmap(b, dBC, dTC, colors);

            int l = colors[0];
            int lt = colors[1];
            int s = colors[2];
            int st = colors[3];

            l = l == dBC ? s : l;
            lt = l == s ? st : lt;

            holder.text.setText(text);
            holder.text.setTextColor(lt);
            holder.text.setBackgroundColor(l);

            holder.image.setImageBitmap(b);

            if (listener != null) {
                final int pos = position;
                holder.item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(pos, url[pos], texts[pos]);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return texts.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView text;
            View item;

            public ViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.me_social_item_image);
                text = (TextView) itemView.findViewById(R.id.me_social_item_text);
                item = itemView;
            }

        }
    }

}
