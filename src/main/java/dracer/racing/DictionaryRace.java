package dracer.racing;

import dracer.racing.words.DictionaryWord;
import net.dv8tion.jda.api.entities.Member;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface DictionaryRace {
    enum RaceState {
        STARTING, IN_PROGRESS, FINISHED
    }

    @Nonnull List<DictionaryWord> getWords();

    @Nonnull List<Member> getPlayers();

    @Nullable Member getWinner();
}
