package tv.piratemedia.connection;

import static tv.piratemedia.connection.Interface.DATA_PID;
import static tv.piratemedia.connection.Interface.DATA_PREF;
import static tv.piratemedia.connection.Interface.DATA_SHOT;
import static tv.piratemedia.connection.Interface.DATA_TEMPERATURE;
import static tv.piratemedia.connection.Interface.MSG_CONNECTED;
import static tv.piratemedia.connection.Interface.MSG_DISCONNECTED;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import tv.piratemedia.ui.settings.RemoteSettingsUtils;

public class GattConnection extends BluetoothGattCallback {
    private Handler handler;
    private Context context;
    public BluetoothGattCharacteristic characteristic;
    private StringBuffer inputBuffer;
    private OnReady onReady;

    public GattConnection(Context context, Handler handler, OnReady onReady) {
        this.context = context;
        this.handler = handler;
        this.onReady = onReady;
        inputBuffer = new StringBuffer();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Message msg = new Message();
        msg.obj = gatt.getDevice().getName();
        if(newState == BluetoothProfile.STATE_CONNECTED) {
            msg.what = MSG_CONNECTED;
            handler.sendMessage(msg);

            gatt.discoverServices();
        }
        if(newState == BluetoothProfile.STATE_DISCONNECTED) {
            msg.what = MSG_DISCONNECTED;
            handler.sendMessage(msg);

            if(characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, false);
                characteristic = null;
            }
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);

        String char_string = characteristic.getStringValue(0);
        inputBuffer.append(char_string);
        parseRawInputData();
    }

    public void parseRawInputData() {
        if(inputBuffer.toString().contains("\n")) {
            int breakIndex = inputBuffer.toString().lastIndexOf("\n");
            String data = inputBuffer.toString().substring(0, breakIndex);
            inputBuffer.delete(0, breakIndex + 1);

            String[] lines = data.split("\\n");
            for(String input : lines) {
                parseInputMessage(input);
            }
        }
    }

    private void parseInputMessage(String message) {
        String[] parts = message.split(" ");

        Message msg = new Message();

        try {
            switch(parts[0]) {
                case "pid":
                    msg.what = DATA_PID;
                    msg.obj = PidInput.fromMessage(message);
                    break;
                case "tmp":
                    msg.what = DATA_TEMPERATURE;
                    msg.obj = TemperatureInput.fromMessage(message);
                    break;
                case "sht":
                    msg.what = DATA_SHOT;
                    msg.obj = ShotInput.fromMessage(message);
                    break;
                case "cmd":
                    msg.what = DATA_PREF;
                    msg.obj = PreferenceInput.fromMessage(context, message);
                    break;
                default:
                    Log.d("GattConnection", "Unknown Message: " + message);
                    return;
            }
        } catch(Exception e) {
            Log.d("Message Parse Error", e.getMessage());
            return;
        }

        handler.sendMessage(msg);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e("GattConnection", "Cant Write to Device");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);

        if (status == BluetoothGatt.GATT_SUCCESS) {
            List<BluetoothGattService> gattServices = gatt.getServices();

            for (BluetoothGattService gattService : gattServices) {
                if(gattService.getUuid().toString().startsWith("0000ffe0")) {
                    if(characteristic != null) {
                        gatt.setCharacteristicNotification(characteristic, false);
                        characteristic = null;
                    }

                    characteristic = gattService.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));

                    if(characteristic == null) {
                        continue;
                    }

                    gatt.setCharacteristicNotification(characteristic, true);
                    characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                    if (!gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)) {
                        Log.e("GattConnection", "Can't get Connection Priority");
                    }

                    onReady.ready(characteristic);
                }
            }
        }
    }

    public interface OnReady {
        void ready(BluetoothGattCharacteristic characteristic);
    }
}
