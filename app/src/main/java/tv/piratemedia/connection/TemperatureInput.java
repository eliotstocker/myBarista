package tv.piratemedia.connection;

public class TemperatureInput {
    public double setPoint;
    public double boiler1;
    public double boiler2;
    public int ts;

    public TemperatureInput(int ts, double setPoint, double boiler1, double boiler2) {
        this.ts = ts;
        this.setPoint = setPoint;
        this.boiler1 = boiler1;
        this.boiler2 = boiler2;
    }

    public static TemperatureInput fromMessage(String input) throws Exception {
        String[] parts = input.split(" ");

        if(!parts[0].equals("tmp")) {
            throw new Exception("input not from temp message");
        }

        int ts = Integer.parseInt(parts[1]);
        double setPoint = Double.parseDouble(parts[2]) / 100;
        double boiler1 = Double.parseDouble(parts[3]) / 100;
        double boiler2 = Double.parseDouble(parts[4]) / 100;

        return new TemperatureInput(
                ts,
                setPoint,
                boiler1,
                boiler2
        );
    }
}
