package tv.piratemedia.myBarista.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import tv.piratemedia.myBarista.R;

public class HardwarePrefsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.hardware, rootKey);
    }
}