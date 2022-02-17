package tv.piratemedia.myBarista.ui.settings;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tv.piratemedia.myBarista.R;
import tv.piratemedia.myBarista.connection.PreferenceInput;

public class RemoteSettingsUtils {
    public final static Map<String, Float> settingMultiplier = Stream.of(new Object[][] {
        {"tmpsp", 100f},
        {"tmpstm", 100f},
        {"pd1i", 100f},
        {"pd1imx", 655.36f},
        {"pistrt", 1000f},
        {"piprd", 1000f}
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Float) data[1]));

    private static final Integer[] preferenceScreens = new Integer[] {R.xml.temp, R.xml.pressure, R.xml.preinfusion, R.xml.timers, R.xml.hardware, R.xml.pid};
    private static final String prefPrefix = "__device_pref_";
    private Context context;

    public RemoteSettingsUtils(Context context) {
        this.context = context;
    }

    @SuppressLint("MissingPermission")
    public static void requestAllDeviceSettings(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String cmd = "\r\ncmd dump OK\r\n";
        characteristic.setValue(cmd);

        if( !gatt.writeCharacteristic(characteristic) )
            Log.e("RemoteSettingsWrite", "Failed: " + cmd);
    }

    @SuppressLint("MissingPermission")
    public static void requestDeviceSetting(String name, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String cmd = "\r\ncmd get " + name + " OK\r\n";
        characteristic.setValue(cmd);

        if( !gatt.writeCharacteristic(characteristic) )
            Log.e("RemoteSettingsWrite", "Failed: " + cmd);
    }

    public static void setupDefaultPrefs(Context context) {
        for(int resource : preferenceScreens) {
            PreferenceManager.setDefaultValues(context, resource, true);
        }
    }

    public PreferenceInput storePreferenceInput(PreferenceInput input) {
        return storePreferenceInput(context, input);
    }

    public static PreferenceInput storePreferenceInput(Context context, PreferenceInput input) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor prefEditor = sharedPreferences.edit();
        switch(input.cls.getTypeName()) {
            case "java.lang.String":
                prefEditor.putString(prefPrefix + input.name, (String) input.value);
                break;
            case "java.lang.Integer":
                prefEditor.putInt(prefPrefix + input.name, (Integer) input.value);
                break;
            case "java.lang.Float":
                prefEditor.putFloat(prefPrefix + input.name, (Float) input.value);
                break;
            case "java.lang.Boolean":
                prefEditor.putBoolean(prefPrefix + input.name, (Boolean) input.value);
                break;
        }

        prefEditor.apply();

        return input;
    }

    public void sendToDeviceFromPreference(String name, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        sendToDeviceFromPreference(context, name, gatt, characteristic);
    }

    @SuppressLint("MissingPermission")
    public static void sendToDeviceFromPreference(Context context, String name, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        Object pref = sharedPreferences.getAll().get(name);

        String actualName = name.substring("__device_pref_".length());
        String sendValue = "";

        if(pref == null) {
            return;
        }

        switch(pref.getClass().getName()) {
            case "java.lang.String":
                sendValue = (String) pref;
                break;
            case "java.lang.Boolean":
                sendValue = (boolean)pref ? "1" : "0";
                break;
            case "java.lang.Float":
                sendValue = ((Integer)Math.round((float)pref * (settingMultiplier.getOrDefault(actualName, 1f)))).toString();
                break;
            case "java.lang.Integer":
                sendValue = pref.toString();
                break;
        }

        String cmd = "cmd set " + actualName + " " + sendValue + " OK\r\n";
        characteristic.setValue(cmd);

        gatt.beginReliableWrite();
        if(!gatt.writeCharacteristic(characteristic))
            Log.e("RemoteSettingsWrite", "Failed: " + cmd);
    }
}
