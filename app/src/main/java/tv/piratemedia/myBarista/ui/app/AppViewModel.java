package tv.piratemedia.myBarista.ui.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.preference.PreferenceManager;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import tv.piratemedia.myBarista.connection.PidInput;
import tv.piratemedia.myBarista.connection.ShotInput;
import tv.piratemedia.myBarista.connection.TemperatureInput;

public class AppViewModel extends AndroidViewModel {
    private final static MutableLiveData<String> connectionStatus = new MutableLiveData<>("Disconnected");
    private final static MutableLiveData<ArrayList<TemperatureInput>> temperatureInfo = new MutableLiveData<>(new ArrayList<>());
    private final static MutableLiveData<Float> pidPower = new MutableLiveData<>(0f);
    private final static MutableLiveData<Boolean> showTimer = new MutableLiveData<>(false);
    private final static MutableLiveData<Double> shotTime = new MutableLiveData<>(0.0);
    private final static TimerHandler tHandler = new TimerHandler();
    private static Timer timer;
    private static long timerStart;

    public AppViewModel(@NonNull Application application) {
        super(application);
    }

    private static class TimerHandler extends Handler {
        public TimerHandler(){
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            double val = (double) msg.obj;
            shotTime.setValue(val);
            super.handleMessage(msg);
        }
    }

    public static void addTempInfo(TemperatureInput info) {
        ArrayList<TemperatureInput> tempInfo = temperatureInfo.getValue();
        if (tempInfo != null && tempInfo.size() > 0 && info.ts < tempInfo.get(tempInfo.size() -1).ts - 5) {
            tempInfo = new ArrayList<>();
        }

        assert tempInfo != null;
        if(tempInfo.size() < 1) {
            tempInfo.add(new TemperatureInput(info.ts - 100, info.setPoint, info.boiler1, info.boiler2));
        }

        tempInfo.add(info);

        temperatureInfo.setValue(tempInfo);
    }

    public static void setPidPower(PidInput info) {
        pidPower.setValue(info.boilerPower);
    }

    public static void setConnectionStatus(String status) {
        connectionStatus.setValue(status);
    }

    public static void hideTimer(View view) {
        hideTimer();
    }

    public static void hideTimer() {
        showTimer.setValue(false);
    }

    public static void setShotTime(ShotInput info) {
        if(info.status.equals("start")) {
            showTimer.setValue(true);

            if(timer != null) {
                timer.cancel();
                timer.purge();
            }
            timer = new Timer();

            timerStart = System.currentTimeMillis();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Message m = new Message();
                    m.obj = (System.currentTimeMillis() - timerStart) / 1000.0;
                    tHandler.sendMessage(m);
                }
            }, 100, 100);
        } else if(info.status.equals("end")) {
            if(timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            shotTime.setValue((System.currentTimeMillis() - timerStart) / 1000.0);
        }
    }

    public LiveData<Integer> getPower() {
        return Transformations.map(pidPower, Math::round);
    }

    public LiveData<String> getPowerDisplay() {
        return Transformations.map(pidPower, power -> Math.round(power) + "%");
    }

    public LiveData<LineData> getTempHistory() {
        return Transformations.map(temperatureInfo, data -> {
            //boiler1 temp
            List<Entry> tempEntries = data.stream().map(tInput -> new Entry(tInput.ts, (float) tInput.boiler1)).collect(Collectors.toList());
            LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature");
            setLineStyles(tempDataSet, true, true);

            //setpoint
            List<Entry> setpointEntries = data.stream().map(tInput -> new Entry(tInput.ts, (float) tInput.setPoint)).collect(Collectors.toList());
            LineDataSet setpointDataSet = new LineDataSet(setpointEntries, "Set Point");
            setpointDataSet.setColor(Color.RED);
            setLineStyles(setpointDataSet, false, false);

            return new LineData(setpointDataSet, tempDataSet);
        });
    }

    public LiveData<Integer> getMinChartValue() {
        return Transformations.map(temperatureInfo, data -> data.size() > 0 ? (int) Math.round(data.get(data.size() - 1).boiler1) - 20 : 0);
    }

    public LiveData<String> getCurrentTemp() {
        return Transformations.map(temperatureInfo, data -> data.size() > 0 ? String.format("%1$,.2f", data.get(data.size() - 1).boiler1) : "No Data...");
    }

    public LiveData<Float> getCurrentSetpoint() {
        return Transformations.map(temperatureInfo, data -> data.size() > 0 ? (float) data.get(data.size() - 1).setPoint : 50f);
    }

    public LiveData<Integer> getCurrentUIVisibility() {
        return Transformations.map(connectionStatus, data -> data.startsWith("Connected:") ? View.VISIBLE : View.GONE);
    }

    public LiveData<Integer> getCurrentStatusVisibility() {
        return Transformations.map(connectionStatus, data -> data.startsWith("Connected:") ? View.GONE : View.VISIBLE);
    }

    public LiveData<String> getStatusProgress() {
        return Transformations.map(connectionStatus, data -> data.split(":")[0]);
    }

    public LiveData<String> getStatusInfo() {
        return Transformations.map(connectionStatus, data -> data.contains(":") ? data.split(":")[1] : "Please turn on Coffee Machine");
    }

    public LiveData<Integer> getShotTimerVisibility() {
        return Transformations.map(showTimer, data -> data ? View.VISIBLE : View.GONE);
    }

    public LiveData<String> getShotTimerValue() {
        return Transformations.map(shotTime, data -> "T: " + String.format("%1$,.1f", data));
    }

    public LiveData<Integer> getShotTimerSecondaryVisibility() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplication());
        return Transformations.map(shotTime, data -> sp.getBoolean("__device_pref_pinbl", true) ? View.VISIBLE : View.GONE);
    }

    public LiveData<String> getShotTimerSecondaryValue() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getApplication());

        return Transformations.map(shotTime, data -> {
            if(sp.getBoolean("__device_pref_pinbl", true)) {
                float preInfusionTime = sp.getFloat("__device_pref_pistrt", 3.0f);
                float preInfusionWait = sp.getFloat("__device_pref_piprd", 3.0f);

                if(data < preInfusionTime + preInfusionWait) {
                    return "S: 0.0";
                } else {
                    return "S: " + String.format("%1$,.1f", data - (preInfusionTime + preInfusionWait));
                }

            }
            return "S: 0.0";
        });
    }

    private void setLineStyles(LineDataSet line, boolean filled, boolean interpolated) {
        line.setDrawCircles(false);
        line.setMode(interpolated? LineDataSet.Mode.CUBIC_BEZIER : LineDataSet.Mode.STEPPED);
        line.setDrawFilled(filled);
        line.setDrawValues(false);
    }
}