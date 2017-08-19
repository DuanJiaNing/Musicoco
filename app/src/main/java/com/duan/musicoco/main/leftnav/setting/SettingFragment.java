package com.duan.musicoco.main.leftnav.setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.duan.musicoco.R;

/**
 * Created by DuanJiaNing on 2017/8/18.
 */

public class SettingFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
    }
}
