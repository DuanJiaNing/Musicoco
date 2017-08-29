package com.duan.musicoco.setting;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.duan.musicoco.R;
import com.duan.musicoco.preference.SettingPreference;

/**
 * Created by DuanJiaNing on 2017/8/18.
 */

public class SettingFragment extends PreferenceFragment {

    AutoSwitchThemeController as;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        as = AutoSwitchThemeController.getInstance(getActivity());

        findPreference("pre_auto_switch_night_theme").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                checkAutoThemeSwitch();
                return false;
            }
        });

    }

    private void checkAutoThemeSwitch() {
        Context context = getActivity();
        SettingPreference settingPreference = new SettingPreference(context);
        if (settingPreference.autoSwitchNightTheme() && !as.isSet()) {
            as.setAlarm();
        } else {
            as.cancelAlarm();
        }
    }
}
