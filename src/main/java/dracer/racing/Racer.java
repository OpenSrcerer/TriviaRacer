package dracer.racing;

import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nullable;

public class Racer {
    public final Member member;
    private int wordsTyped = 0;

    public Racer(Member member) {
        this.member = member;
    }

    @Nullable
    public static Racer getNullRacer() {
        return null;
    }

    public void incrementWords() {
        ++wordsTyped;
    }

    public int getWordsTyped() {
        return wordsTyped;
    }
}
