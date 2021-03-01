package dracer.util;

import dracer.TRacer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * For race timekeeping.
 */
public class RaceTime {
    private Instant raceCall; // When the RaceStart embed is sent
    private Instant startOfRace; // Instant when the grace period ends.
    private Instant endOfRace; // When the time runs out.
    private boolean temporalsSet = false;

    public void setTemporals() {
        this.raceCall = Instant.now();
        this.startOfRace = Instant.now().plusSeconds(TRacer.GRACE_PERIOD);
        this.endOfRace = Instant.now().plusSeconds(TRacer.TOTAL_LENGTH);
        temporalsSet = true;
    }

    // Call during grace
    public long getSecondsToStartOfRace() {
        return ChronoUnit.SECONDS.between(Instant.now(), startOfRace);
    }

    public long getSecondsPreGrace(int secondsBefore) {
        return ChronoUnit.SECONDS.between(raceCall, startOfRace.minusSeconds(secondsBefore));
    }

    // Call while in progress
    public long getSecondsPreEndOfRace(int secondsBefore) {
        return ChronoUnit.SECONDS.between(raceCall, endOfRace.minusSeconds(secondsBefore));
    }

    public boolean isTemporalsSet() {
        return temporalsSet;
    }
}
