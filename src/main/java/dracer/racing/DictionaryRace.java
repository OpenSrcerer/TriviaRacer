package dracer.racing;

import dracer.racing.entities.Racer;
import dracer.racing.words.DictionaryWord;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.Callback;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface DictionaryRace extends Callback {
    // --- Const ---
    int RACE_TASKS = 10;
    // -----------------

    enum RaceState {
        STARTING, IN_PROGRESS, FINISHED
    }

    void incrementWords(String racerId);

    void setState(RaceState state);

    void setEndFuture(ScheduledFuture<Void> future);

    void setMessage(Message message);

    void setTime();

    void cancelFuture();

    boolean addRacer(Member member);

    boolean removeRacer(String racerId);

    boolean isCancelled();

    @Nonnull List<DictionaryWord> getWords();

    @Nonnull List<Racer> getPlayers();

    @Nonnull RaceState getState();

    @Nonnull RaceTime getTime();

    @Nonnull Message getMessage();

    @Nonnull String getLeaderboard();

    @Nonnull String getGuildId();

    @Nonnull String getChannelId();

    @Nonnull String getEmojID();
}
