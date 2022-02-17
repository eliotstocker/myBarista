package tv.piratemedia.myBarista.connection;

import static tv.piratemedia.myBarista.connection.Interface.MSG_FOUND;
import static tv.piratemedia.myBarista.connection.Interface.MSG_SEARCHING;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Collections;
import java.util.UUID;

import tv.piratemedia.myBarista.ui.settings.RemoteSettingsUtils;

public class BLE {
    private Context context;
    private Handler handler;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic bluetoothCharacteristic;

    public BLE(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }

        return bluetoothAdapter;
    }

    private BluetoothLeScanner getBluetoothLeScanner() {
        if (bluetoothLeScanner == null)
            bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();

        return bluetoothLeScanner;
    }

    @SuppressLint("MissingPermission")
    public void discover() {
        handler.sendEmptyMessage(MSG_SEARCHING);

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")))
                .build();

        getBluetoothLeScanner().startScan(Collections.singletonList(scanFilter), scanSettings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if(bluetoothDevice == null) {
                    bluetoothDevice = result.getDevice();

                    bluetoothLeScanner.stopScan(this);
                    notifyDeviceFound(bluetoothDevice);

                    bluetoothGatt = bluetoothDevice.connectGatt(context.getApplicationContext(), false, new GattConnection(context, handler, characteristic -> {
                        bluetoothCharacteristic = characteristic;
                        RemoteSettingsUtils.requestAllDeviceSettings(bluetoothGatt, characteristic);
                    }));
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void notifyDeviceFound(BluetoothDevice device) {
        Message msg = new Message();
        msg.what = MSG_FOUND;
        msg.obj = device.getName();

        handler.sendMessage(msg);
    }

    @SuppressLint("MissingPermission")
    public void close() {
        if(bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        bluetoothGatt = null;
        bluetoothDevice = null;
    }

    public void syncPref(String name) {
        if(bluetoothGatt == null || bluetoothCharacteristic == null) {
            Log.e("BLE", "Attempted to sync pref before connection ready");
            return;
        }

        RemoteSettingsUtils.sendToDeviceFromPreference(context, name, bluetoothGatt, bluetoothCharacteristic);
    }
}
