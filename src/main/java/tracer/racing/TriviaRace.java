package tracer.racing;

import tracer.racing.entities.Racer;
import tracer.racing.tasks.Task;
import tracer.util.RaceTime;
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

    void setMessage(Message message);

    void setTasks(List<Task> tasks);

    void setTime();

    void incrementCurrentTask();

    void addActions(List<ScheduledFuture<?>> future);

    void cancel();

    void removeRacer(String racerId);

    void addRacer(Member member);

    boolean evalAnswer(String racerId, String emoji);

    boolean isCancelled();

    @Nonnull List<Task> getTasks();

    @Nonnull List<Racer> getPlayers();

    @Nonnull Task.TaskCategory getCategory();

    @Nonnull RaceState getState();

    @Nonnull RaceTime getTime();

    @Nonnull Message getMessage();

    @Nonnull TextChannel getChannel();

    @Nonnull String getLeaderboard();

    @Nonnull String getOwnerId();

    @Nonnull String getChannelId();

    @Nonnull String getEmojID();

    int getCurrentTask();
}
