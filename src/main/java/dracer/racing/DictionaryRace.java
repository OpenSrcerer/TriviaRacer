package dracer.racing;

import dracer.racing.words.DictionaryWord;
import net.dv8tion.jda.api.entities.Member;
import okhttp3.Callback;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface DictionaryRace extends Callback {
    static DictionaryRace getRace(Member startingMember) {
        return new DictionaryRaceImpl(startingMember);
    }

    enum RaceState {
        STARTING, IN_PROGRESS, FINISHED
    }

    void joinStartingGame(Member member);

    void setState(RaceState state);

    void setWinner(String userId);

    @Nonnull List<DictionaryWord> getWords();

    @Nonnull List<Racer> getPlayers();

    @Nonnull RaceState getState();

    @Nullable Racer getWinner();
}
