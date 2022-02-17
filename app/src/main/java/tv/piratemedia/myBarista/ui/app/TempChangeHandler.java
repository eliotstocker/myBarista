package tv.piratemedia.myBarista.ui.app;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import it.sephiroth.android.library.numberpicker.NumberPicker;
import tv.piratemedia.myBarista.MainActivity;

public class TempChangeHandler implements NumberPicker.OnNumberPickerChangeListener {
    private Activity activity;

    public TempChangeHandler(@NonNull Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onProgressChanged(@NonNull NumberPicker numberPicker, float v, boolean b) {
        String key = "__device_pref_tmpsp";
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        prefEditor.putFloat(key, v);
        prefEditor.commit();

        ((MainActivity)activity).sendRemotePrefValue(key);
    }

    @Override
    public void onStartTrackingTouch(@NonNull NumberPicker numberPicker) {

    }

    @Override
    public void onStopTrackingTouch(@NonNull NumberPicker numberPicker) {

    }
}
