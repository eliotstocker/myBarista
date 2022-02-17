package tv.piratemedia;

import static tv.piratemedia.connection.Interface.DATA_PID;
import static tv.piratemedia.connection.Interface.DATA_PREF;
import static tv.piratemedia.connection.Interface.DATA_SHOT;
import static tv.piratemedia.connection.Interface.DATA_TEMPERATURE;
import static tv.piratemedia.connection.Interface.MSG_CONNECTED;
import static tv.piratemedia.connection.Interface.MSG_DISCONNECTED;
import static tv.piratemedia.connection.Interface.MSG_FOUND;
import static tv.piratemedia.connection.Interface.MSG_SEARCHING;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;

import tv.piratemedia.connection.PidInput;
import tv.piratemedia.connection.PreferenceInput;
import tv.piratemedia.connection.ShotInput;
import tv.piratemedia.connection.TemperatureInput;

public class ConnectionMessageHandler extends Handler {
    private OnTemperatureDataReceived onTemperatureDataReceived;
    private OnPidDataReceived onPidDataReceived;
    private OnShotDataReceived onShotDataReceived;
    private OnPreferenceDataReceived onPreferenceDataReceived;
    private OnStatusChange onStatusChange;

    public ConnectionMessageHandler(OnTemperatureDataReceived onTemperatureDataReceived, OnPidDataReceived onPidDataReceived, OnShotDataReceived onShotDataReceived, OnPreferenceDataReceived onPreferenceDataReceived, OnStatusChange onStatusChange) {
        super(Looper.getMainLooper());
        this.onTemperatureDataReceived = onTemperatureDataReceived;
        this.onPidDataReceived = onPidDataReceived;
        this.onStatusChange = onStatusChange;
        this.onShotDataReceived = onShotDataReceived;
        this.onPreferenceDataReceived = onPreferenceDataReceived;
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        switch(msg.what) {
            case MSG_SEARCHING:
                onStatusChange.onStatus("Searching");
                break;
            case MSG_FOUND:
                onStatusChange.onStatus("Connecting:" + msg.obj.toString());
                break;
            case MSG_CONNECTED:
                onStatusChange.onStatus("Connected:" + msg.obj.toString());
                onStatusChange.OnConnected();
                break;
            case MSG_DISCONNECTED:
                onStatusChange.onStatus("Disconnected:" + msg.obj.toString());
                onStatusChange.onDisconnect();
                break;
            case DATA_TEMPERATURE:
                TemperatureInput tInput = (TemperatureInput) msg.obj;
                onTemperatureDataReceived.onData(tInput);
                break;
            case DATA_PID:
                PidInput pInput = (PidInput) msg.obj;
                onPidDataReceived.onData(pInput);
                break;
            case DATA_SHOT:
                ShotInput sInput = (ShotInput) msg.obj;
                onShotDataReceived.onData(sInput);
                break;
            case DATA_PREF:
                PreferenceInput prefInput = (PreferenceInput) msg.obj;
                onPreferenceDataReceived.onData(prefInput);
            default:
                super.handleMessage(msg);
                break;
        }
    }

    public interface OnTemperatureDataReceived {
        void onData(TemperatureInput data);
    }

    public interface OnPidDataReceived {
        void onData(PidInput data);
    }

    public interface OnShotDataReceived {
        void onData(ShotInput data);
    }

    public interface OnPreferenceDataReceived {
        void onData(PreferenceInput data);
    }

    public interface OnStatusChange {
        void onStatus(String status);
        void onDisconnect();
        void OnConnected();
    }
}
