package tv.piratemedia.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private final static MutableLiveData<Boolean> isConnected = new MutableLiveData<>(false);

    public static void setConnected(boolean connected) {
        isConnected.setValue(connected);
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }
}
