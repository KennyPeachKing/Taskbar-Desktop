/* Copyright 2020 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.farmerbb.taskbar.fragment;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.provider.Settings;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.farmerbb.taskbar.R;
import com.farmerbb.taskbar.activity.HSLActivity;
import com.farmerbb.taskbar.activity.HSLConfigActivity;
import com.farmerbb.taskbar.activity.SecondaryHomeActivity;
import com.farmerbb.taskbar.util.U;

public class DesktopModeFragment extends SettingsFragment {

    public static boolean isConfiguringHomeApp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        finishedLoadingPrefs = false;

        super.onCreate(savedInstanceState);

        // Add preferences
        addPreferencesFromResource(R.xml.tb_pref_desktop_mode);

        // Set OnClickListeners for certain preferences
        findPreference("desktop_mode").setOnPreferenceClickListener(this);
        findPreference("set_launcher_default").setOnPreferenceClickListener(this);
        findPreference("primary_launcher").setOnPreferenceClickListener(this);

        SharedPreferences pref = U.getSharedPreferences(getActivity());
        if(pref.getBoolean("launcher", false))
            findPreference("desktop_mode").setEnabled(false);
        else
            bindPreferenceSummaryToValue(findPreference("desktop_mode"));

        bindPreferenceSummaryToValue(findPreference("display_density"));

        boolean writeSecureSettings = U.hasWriteSecureSettingsPermission(getActivity());
        findPreference("display_density").setEnabled(writeSecureSettings);
        findPreference("auto_hide_navbar").setEnabled(writeSecureSettings);

        finishedLoadingPrefs = true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setTitle(R.string.tb_pref_header_desktop_mode);
        ActionBar actionBar = activity.getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        isConfiguringHomeApp = false;

        Preference primaryLauncherPref = findPreference("primary_launcher");
        if(primaryLauncherPref != null) {
            SharedPreferences pref = U.getSharedPreferences(getActivity());
            String primaryLauncherName = pref.getString("hsl_name", "null");
            String primaryLauncherPackage = pref.getString("hsl_id", "null");

            boolean primaryLauncherValid = true;
            try {
                getActivity().getPackageManager().getPackageInfo(primaryLauncherPackage, 0);
            } catch (PackageManager.NameNotFoundException e) {
                primaryLauncherValid = false;
            }

            primaryLauncherPref.setSummary(primaryLauncherValid
                    ? primaryLauncherName
                    : getString(R.string.tb_icon_pack_none)
            );
        }
    }

    @TargetApi(29)
    @Override
    public boolean onPreferenceClick(final Preference p) {
        switch(p.getKey()) {
            case "desktop_mode":
                U.setComponentEnabled(getActivity(), SecondaryHomeActivity.class,
                        ((CheckBoxPreference) p).isChecked());

                U.setComponentEnabled(getActivity(), HSLActivity.class,
                        ((CheckBoxPreference) p).isChecked());

               break;
            case "set_launcher_default":
                try {
                    startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
                    isConfiguringHomeApp = true;
                } catch (ActivityNotFoundException e) {
                    U.showToastLong(getActivity(), R.string.tb_unable_to_set_default_home);
                }

                break;
            case "primary_launcher":
                Intent intent = new Intent(getActivity(), HSLConfigActivity.class);
                intent.putExtra("return_to_settings", true);
                startActivity(intent);

                break;
        }

        return super.onPreferenceClick(p);
    }
}