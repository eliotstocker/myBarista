package tv.piratemedia.connection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.lang.reflect.Type;
import java.util.Arrays;

import tv.piratemedia.ui.settings.RemoteSettingsUtils;

public class PreferenceInput {

    public String type;
    public String name;
    public Type cls;
    public Object value;

    public PreferenceInput(String type, String name, Integer value) {
        this.cls = Integer.class;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public PreferenceInput(String type, String name, Float value) {
        this.cls = Float.class;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public PreferenceInput(String type, String name, Boolean value) {
        this.cls = Boolean.class;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public PreferenceInput(String type, String name, String value) {
        this.cls = String.class;
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public static PreferenceInput fromMessage(Context ctx, String input) throws Exception {
        String[] parts = input.split(" ");

        if(!parts[0].equals("cmd")) {
            throw new Exception("input not from preference message");
        }

        String type = parts[1];
        if(!type.equals("get") && !type.equals("set")) {
            throw new Exception("input not get or set command");
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);

        String name = parts[2];

        if(!settings.getAll().containsKey("__device_pref_" + name)) {
            throw new Exception("unknown preference: " + name);
        }

        Object currentSetting = settings.getAll().get("__device_pref_" + name);

        if(currentSetting == null) {
            throw new Exception("Preference type unknown: " + name);
        }

        switch(currentSetting.getClass().getName()) {
            case "java.lang.String":
                return new PreferenceInput(type, name, parts[3]);
            case "java.lang.Boolean":
                return new PreferenceInput(type, name, Integer.parseInt(parts[3]) != 0);
            case "java.lang.Float":
                return new PreferenceInput(type, name, Float.parseFloat(parts[3]) / (RemoteSettingsUtils.settingMultiplier.getOrDefault(name, 1f)));
            case "java.lang.Integer":
                return new PreferenceInput(type, name, Integer.parseInt(parts[3]));
        }

        throw new Exception("Preference type unknown: " + name);
    }

    @NonNull
    @Override
    public String toString() {
        return "PreferenceInput{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", value=" + value + "(" + cls.getTypeName() + ")" +
                '}';
    }
}
