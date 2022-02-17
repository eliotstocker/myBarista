package tv.piratemedia.connection;

public class ShotInput {
    public String status;
    public int time;

    public ShotInput(String status, int time) {
        this.status = status;
        this.time = time;
    }

    public static ShotInput fromMessage(String input) throws Exception {
        String[] parts = input.split(" ");

        if(!parts[0].equals("sht")) {
            throw new Exception("input not from shot message");
        }

        int time = Integer.parseInt(parts[2]);

        String status;
        if(time == 0) {
            status = "start";
        } else {
            status = "end";
        }

        return new ShotInput(status, time);
    }
}
