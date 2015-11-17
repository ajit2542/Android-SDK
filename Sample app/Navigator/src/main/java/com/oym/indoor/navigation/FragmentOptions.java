package com.oym.indoor.navigation;

import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

/**
 * Created by joan on 29/10/15.
 */
public class FragmentOptions extends PreferenceFragmentCompat {

    private GlobalState gs;

    private ListPreference locAlgo;
    private CheckBoxPreference forceScan;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_options);

        gs = (GlobalState) getActivity().getApplication();

        locAlgo = (ListPreference) findPreference(getString(R.string.FPOLocationAlgKey));
        forceScan = (CheckBoxPreference) findPreference(getString(R.string.FPOForceScanKey));

        locAlgo.setSummary(getString(R.string.FPOLocationAlgSummary, locAlgo.getEntry()));
        
        locAlgo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String indexStr = (String) o;
                int index = Integer.parseInt(indexStr);
                String type = getResources().getStringArray(R.array.FPOLocationTypes)[index];
                locAlgo.setSummary(getString(R.string.FPOLocationAlgSummary, type));
                //noinspection ResourceType
                gs.getGoIndoor().setLocationType(index);
                return true;
            }
        });
        forceScan.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean value = (boolean) o;
                gs.getGoIndoor().setScanForced(value);
                forceScan.setChecked(value);
                return true;
            }
        });

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.primaryColorInverse));
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }
}
