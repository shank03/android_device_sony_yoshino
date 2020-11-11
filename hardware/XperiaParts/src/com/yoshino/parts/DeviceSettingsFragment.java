/*
 * Copyright (c) 2020, Shashank Verma (shank03) <shashank.verma2002@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package com.yoshino.parts;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import static com.yoshino.parts.Constants.*;

public class DeviceSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle bundle, String key) {
        addPreferencesFromResource(R.xml.device_settings);

        String buildFlavor = SystemProperties.get("ro.build.flavor", "havoc_lilac");
        if (buildFlavor.contains("aosp")) {
            PreferenceCategory cameraCategory = findPreference("camera_key");
            if (cameraCategory != null) {
                cameraCategory.setVisible(false);
            }
        }

        SwitchPreference cameraPref = findPreference(CAMERA_LONG_PRESS);
        if (cameraPref != null) {
            Settings.System.putInt(cameraPref.getContext().getContentResolver(), CAMERA_LONG_PRESS,
                    cameraPref.isChecked() ? 1 : 0);
            cameraPref.setOnPreferenceChangeListener(this);
        }

        SwitchPreference focusPref = findPreference(FOCUS_TOGGLE_FLASH);
        if (focusPref != null) {
            Settings.System.putInt(focusPref.getContext().getContentResolver(), FOCUS_TOGGLE_FLASH,
                    focusPref.isChecked() ? 1 : 0);
            focusPref.setOnPreferenceChangeListener(this);
        }

        SwitchPreference glovePref = findPreference(GLOVE_MODE);
        if (glovePref != null) {
            Settings.System.putInt(glovePref.getContext().getContentResolver(), GLOVE_MODE,
                    glovePref.isChecked() ? 1 : 0);
            glovePref.setOnPreferenceChangeListener(this);
        }

        SwitchPreference notificationPref = findPreference(NS_NOTIFICATION);
        if (notificationPref != null) {
            Settings.System.putInt(notificationPref.getContext().getContentResolver(), NS_NOTIFICATION,
                    notificationPref.isChecked() ? 1 : 0);
            notificationPref.setOnPreferenceChangeListener(this);
        }

        Preference statusPref = findPreference(NS_STATUS);
        if (statusPref != null) {
            statusPref.setSummary(Settings.System.getString(statusPref.getContext().getContentResolver(), NS_STATUS));
        }

        Preference logPref = findPreference("ns_log");
        if (logPref != null) {
            logPref.setOnPreferenceClickListener(preference -> {
                preference.getContext().startActivity(new Intent()
                        .setClassName("com.yoshino.networkswitcher", "com.yoshino.networkswitcher.LogActivity"));
                return true;
            });
        }

        Preference msActPref = findPreference(MODEM_SWITCHER);
        if (msActPref != null) {
            msActPref.setOnPreferenceClickListener(preference -> {
                preference.getContext().startActivity(new Intent()
                        .setClassName("com.sonymobile.customizationselector", "com.sonymobile.customizationselector.ModemSwitcherActivity"));
                return true;
            });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case CAMERA_LONG_PRESS:
                Settings.System.putInt(preference.getContext().getContentResolver(), CAMERA_LONG_PRESS, (boolean) o ? 1 : 0);
                return true;
            case FOCUS_TOGGLE_FLASH:
                Settings.System.putInt(preference.getContext().getContentResolver(), FOCUS_TOGGLE_FLASH, (boolean) o ? 1 : 0);
                return true;
            case GLOVE_MODE:
                Settings.System.putInt(preference.getContext().getContentResolver(), GLOVE_MODE, (boolean) o ? 1 : 0);
                SystemProperties.set(GLOVE_PROP, (boolean) o ? "1" : "0");
                return true;
            case NS_NOTIFICATION:
                Settings.System.putInt(preference.getContext().getContentResolver(), NS_NOTIFICATION, (boolean) o ? 1 : 0);
                return true;
        }
        return false;
    }
}
