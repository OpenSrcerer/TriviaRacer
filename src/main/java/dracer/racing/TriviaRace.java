package dracer.racing;

import dracer.racing.entities.Racer;
import dracer.racing.tasks.Task;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public interface TriviaRace {
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

    void incrementCurrentTask();

    boolean evalAnswer(String racerId, String answer);

    boolean addRacer(Member member);

    boolean removeRacer(String racerId);

    boolean isCancelled();

    @Nonnull List<Task> getTasks();

    @Nonnull List<Racer> getPlayers();

    @Nonnull Task.TaskCategory getCategory();

    @Nonnull RaceState getState();

    @Nonnull RaceTime getTime();

    @Nonnull Message getMessage();

    @Nonnull TextChannel getChannel();

    @Nonnull String getLeaderboard();

    @Nonnull String getChannelId();

    @Nonnull String getEmojID();

    int getCurrentTask();
}
