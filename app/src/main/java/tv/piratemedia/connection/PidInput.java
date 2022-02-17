package tv.piratemedia.connection;

import android.util.Log;

public class PidInput {
    public float boilerPower;

    public PidInput(float boilerPower) {
        this.boilerPower = boilerPower;
    }

    public static PidInput fromMessage(String input) throws Exception {
        String[] parts = input.split(" ");

        if(!parts[0].equals("pid")) {
            throw new Exception("input not from pid message");
        }

        int rawPower = Integer.parseInt(parts[1]) + Integer.parseInt(parts[2]) + Integer.parseInt(parts[3]);

        if(parts.length >= 5 && !parts[4].equals("OK"))
            rawPower += Integer.parseInt(parts[4]);

        float powerPercent = rawPower / 65535f * 100.0f;

        if(powerPercent > 100f) {
            powerPercent = 100f;
        }

        return new PidInput(powerPercent);
    }
}
