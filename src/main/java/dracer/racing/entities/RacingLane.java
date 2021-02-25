package dracer.racing.entities;

import dracer.racing.DictionaryRaceImpl;

public class RacingLane {
    private static final StringBuilder raceTrack = new StringBuilder(
            "                                                                                 " +
            "                                                                                                         " +
            "          ");

    private final String vehicle;
    private float position;

    public RacingLane(String vehicle) {
        this.vehicle = vehicle;
        this.position = 0;
    }

    public void incrementPosition() {
        position = position + (1f / DictionaryRaceImpl.RACE_TASKS) * raceTrack.length();
    }

    public String getLane() {
        if (position >= raceTrack.length() - 1) {
            return raceTrack.substring(0, raceTrack.length()).concat(vehicle);
        }

        return vehicleAtPosition((int) position).concat(":checkered_flag:");
    }

    public String vehicleAtPosition(int position) {
        StringBuilder newString = new StringBuilder();

        for (int i = 0; i < raceTrack.length(); ++i) {
            newString.append(raceTrack.charAt(i));

            if (i == position) {
                newString.append(vehicle);
                newString.append(raceTrack.substring(i, raceTrack.length()));
                return newString.toString();
            }
        }
        return newString.toString();
    }
}
