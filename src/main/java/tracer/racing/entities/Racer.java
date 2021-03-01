package tracer.racing.entities;

import tracer.TRacer;
import net.dv8tion.jda.api.entities.Member;

public class Racer {
    public final Member member;
    public final RacingLane lane;
    private int tasksCompleted = 0;

    public Racer(Member member) {
        this.member = member;
        this.lane = new RacingLane(TRacer.getRandomCar());
    }

    public void plusTasksCompleted() {
        ++tasksCompleted;
        lane.incrementPosition();
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }
}
