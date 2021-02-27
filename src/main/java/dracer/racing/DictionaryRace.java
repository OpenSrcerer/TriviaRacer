package dracer.racing;

import dracer.racing.entities.Racer;
import dracer.racing.tasks.Task;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface DictionaryRace {
    // --- Const ---
    int RACE_TASKS = 10;
    // -----------------

    enum RaceState {
        STARTING, IN_PROGRESS, FINISHED
    }

    void setState(RaceState state);

    void setEndFuture(ScheduledFuture<Void> future);

    void setMessage(Message message);

    void setTasks(List<Task> tasks);

    void setTime();

    void cancelFuture();

    boolean addRacer(Member member);

    boolean removeRacer(String racerId);

    boolean isCancelled();

    boolean evalAnswer(String racerId, String answer);

    @Nonnull List<Task> getTasks();

    @Nonnull List<Racer> getPlayers();

    @Nonnull RaceState getState();

    @Nonnull RaceTime getTime();

    @Nonnull Message getMessage();

    @Nonnull String getLeaderboard();

    @Nonnull String getChannelId();

    @Nonnull String getEmojID();
}
