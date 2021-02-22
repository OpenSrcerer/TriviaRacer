package dracer.racing;

import net.dv8tion.jda.api.entities.Member;

public class Racer {
    public final Member member;
    public int wordsTyped = 0;

    public Racer(Member member) {
        this.member = member;
    }
}
