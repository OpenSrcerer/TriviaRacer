package dracer.racing.entities;

import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nullable;

public class Racer {
    public final Member member;
    public final RacingLane lane;
    private int wordsTyped = 0;

    public Racer(Member member) {
        this.member = member;
        this.lane = new RacingLane("🍕");
    }

    @Nullable
    public static Racer getNullRacer() {
        return null;
    }

    public void incrementWords() {
        ++wordsTyped;
        lane.incrementPosition();
    }

    public int getWordsTyped() {
        return wordsTyped;
    }
}
