package tv.piratemedia.ui.settings.pref;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

import tv.piratemedia.R;

public class TimePreferenceDialog extends PreferenceDialogFragmentCompat {

    private TimePicker mTimePicker;

    public static TimePreferenceDialog newInstance(String key) {
        final TimePreferenceDialog
                fragment = new TimePreferenceDialog();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        mTimePicker = view.findViewById(R.id.time_picker);

        Integer minutesAfterMidnight = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            minutesAfterMidnight = ((TimePreference) preference).getTime();
        }

        if (minutesAfterMidnight != null) {
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;
            boolean is24hour = DateFormat.is24HourFormat(getContext());

            mTimePicker.setIs24HourView(is24hour);
            mTimePicker.setHour(hours);
            mTimePicker.setMinute(minutes);
        }

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> {
            onClick(null, DialogInterface.BUTTON_NEGATIVE);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        view.findViewById(R.id.button_okay).setOnClickListener(v -> {
            onClick(null, DialogInterface.BUTTON_POSITIVE);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hours = mTimePicker.getHour();
            int minutes = mTimePicker.getMinute();

            int minutesAfterMidnight = (hours * 60) + minutes;

            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = (TimePreference) preference;
                if (timePreference.callChangeListener(minutesAfterMidnight)) {
                    timePreference.setTime(minutesAfterMidnight);
                }
            }
        }
    }
}
