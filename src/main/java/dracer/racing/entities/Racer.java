package dracer.racing.entities;

import dracer.TRacer;
import net.dv8tion.jda.api.entities.Member;

public class Racer {
    public final Member member;
    public final RacingLane lane;
    private int tasksCompleted = 0;

    public Racer(Member member) {
        this.member = member;
        this.lane = new RacingLane("🍕");
    }

    public boolean plusTasksCompleted() {
        ++tasksCompleted;
        lane.incrementPosition();
        return tasksCompleted == TRacer.TASK_COUNT;
    }

    public int getTasksCompleted() {
        return tasksCompleted;
    }
}
