package tracer.racing.entities;

public class RacingLane {
    private static final StringBuilder raceTrack = new StringBuilder("          ");
    private static final String tenSpaces = "          ";

    private final String vehicle;
    private float position;

    public RacingLane(String vehicle) {
        this.vehicle = vehicle;
        this.position = 0;
    }

    public void incrementPosition() {
        ++position;
    }

    public String getLane() {
        if (position >= 10) {
            return "<:green_flag:815987669603450942> ".concat("🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥").concat(" " + vehicle);
        }
        return "<:green_flag:815987669603450942> ".concat(buildLaneAtPosition((int) position)).concat(":checkered_flag:");
    }

    public String buildLaneAtPosition(int position) {
        StringBuilder lane = new StringBuilder();
        for (int i = 0; i < raceTrack.length(); ++i) {
            if (i == position) {
                lane.append(vehicle);
            }
            lane.append(tenSpaces);
        }
        return lane.toString();
    }
}
