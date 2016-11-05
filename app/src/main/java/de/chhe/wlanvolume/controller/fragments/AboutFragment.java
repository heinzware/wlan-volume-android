package de.chhe.wlanvolume.controller.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.util.Log;

import de.chhe.wlanvolume.R;

public class AboutFragment extends PreferenceFragment {

    private static final String KEY_VERSION = "preferences_version";

    private PackageInfo packageInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);
        try{
            packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(),0);
        } catch(PackageManager.NameNotFoundException e){
            Log.e(AboutFragment.class.getSimpleName(), e.getMessage());
        }
        setValues(getPreferenceScreen());
    }

    private void setValues(Preference preference){
        if(KEY_VERSION.equals(preference.getKey())){
            if(packageInfo != null){
                preference.setSummary(packageInfo.versionName);
            } else {
                preference.setSummary(getString(R.string.label_version_default));
            }
        }
        //recursive calls to find version-preference
        if (preference instanceof PreferenceGroup) {
            PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
            for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
                setValues(preferenceGroup.getPreference(i));
            }
        }
    }

}
