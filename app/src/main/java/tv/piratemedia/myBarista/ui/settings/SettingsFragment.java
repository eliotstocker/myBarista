package tv.piratemedia.myBarista.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import tv.piratemedia.myBarista.R;

public class SettingsFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Fragment rootFragment = new RootPrefFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.settings_host_main, rootFragment).commit();
    }

    public void setPrefScreen(String fragment) {
        try {
            Class<?> fragClass = Class.forName(fragment);
            Fragment rootFragment = (Fragment) fragClass.newInstance();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

            View contentFrag = requireView().findViewById(R.id.settings_host_content);
            if(contentFrag.getVisibility() == View.GONE) {
                contentFrag = requireView().findViewById(R.id.settings_host_main);
            }

            transaction.replace(contentFrag.getId(), rootFragment).commit();
        } catch (Exception ignored) {}
    }
}
