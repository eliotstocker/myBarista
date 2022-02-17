package tv.piratemedia.myBarista.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import tv.piratemedia.myBarista.R;

public class RootPrefFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        SettingsViewModel settingsViewModel =
                new ViewModelProvider(this).get(SettingsViewModel.class);

        settingsViewModel.getIsConnected().observe(getViewLifecycleOwner(), connected -> {
            if(!connected) {
                disableRemoteSettings();
            } else {
                enableAllSettings();
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void disableRemoteSettings() {
        int count = getPreferenceScreen().getPreferenceCount();

        for(int i = 0; i < count; i++) {
            Preference p = getPreferenceScreen().getPreference(i);
            if(p.getKey() == null || !p.getKey().equals("pref_screen_bluetooth")) {
                p.setEnabled(false);
            }
        }
    }

    private void enableAllSettings() {
        int count = getPreferenceScreen().getPreferenceCount();

        for(int i = 0; i < count; i++) {
            Preference p = getPreferenceScreen().getPreference(i);
            p.setEnabled(true);
        }
    }
}