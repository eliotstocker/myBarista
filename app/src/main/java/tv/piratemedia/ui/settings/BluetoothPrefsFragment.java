package tv.piratemedia.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import tv.piratemedia.R;

public class BluetoothPrefsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.bluetooth, rootKey);
    }
}