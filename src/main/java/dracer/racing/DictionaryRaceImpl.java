package dracer.racing;

import dracer.Dracer;
import dracer.racing.entities.Racer;
import dracer.racing.tasks.Task;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public final class DictionaryRaceImpl implements DictionaryRace {

    // --- Immutable ---
    private final Map<String, Racer> players = new HashMap<>();
    private final RaceTime time = new RaceTime(); // Holds the race's length and other timeframes where actions needs to be taken
    private final String channelId;
    private final String emojID = Dracer.getRandomEmojis();
    // -----------------

    // --- Mutable ---
    private ScheduledFuture<Void> raceEndFuture; // A ScheduledFuture that represents when finishSequence() is called.
    private List<Task> raceTasks;
    private RaceState state = RaceState.STARTING; // Race's current state
    private Message message; // Message to refer to and update while racing
    private boolean cancelled = false;
    // -----------------

    protected DictionaryRaceImpl(String channelId, Member startingMember) {
        this.channelId = channelId;
        players.put(startingMember.getId(), new Racer(startingMember));
    }

    // ---- Command Overrides ----
    @Override
    public boolean addRacer(Member member) {
        if (players.get(member.getId()) == null) {
            players.put(member.getId(), new Racer(member));
            return true; // signal success
        }
        return false;
    }

    @Override
    public boolean removeRacer(String racerId) {
        if (players.get(racerId) != null) {
            players.remove(racerId);
            return true; // signal success
        }
        return false;
    }

    @Override
    public void setState(RaceState state) {
        this.state = state;
    }

    @Override
    public void setEndFuture(ScheduledFuture<Void> future) {
        raceEndFuture = future;
    }

    @Override
    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public void setTasks(List<Task> tasks) {
        raceTasks = tasks;
    }

    /**
     * @return True = Race complete due to user completing all tasks.
     *         False = Race continues on.
     */
    @Override
    public boolean evalAnswer(String racerId, String answer) {
        if (players.containsKey(racerId)) {
            for (Task t : raceTasks) {
                if (t.getAnswer().equalsIgnoreCase(answer) && !t.haveCompleted().contains(racerId)) {
                    t.completedBy(racerId);
                    if (players.get(racerId).plusTasksCompleted()) {
                        raceEndFuture.cancel(false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancelFuture() {
        if (raceEndFuture != null) {
            raceEndFuture.cancel(false);
        }
        cancelled = true;
    }

    @Override
    public void setTime() {
        this.time.setTemporals();
    }

    @NotNull
    @Override
    public List<Task> getTasks() {
        return raceTasks;
    }

    @NotNull
    @Override
    public List<Racer> getPlayers() {
        return new ArrayList<>(players.values());
    }

    @NotNull
    @Override
    public RaceState getState() {
        return state;
    }

    @Nonnull
    @Override
    public RaceTime getTime() {
        return time;
    }

    @NotNull
    @Override
    public Message getMessage() {
        return message;
    }

    @NotNull
    @Override
    public String getLeaderboard() {
        StringBuilder leaderboard = new StringBuilder();
        // Calculate the winner
        List<Racer> sortedRacers = getPlayers()
                .stream()
                .sorted(Comparator.comparing(Racer::getTasksCompleted).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < sortedRacers.size(); ++i) {
            leaderboard.append("**#").append(i + 1).append("** <@")
                    .append(sortedRacers.get(i).member.getId())
                    .append("> → Tasks Completed: ")
                    .append(sortedRacers.get(i).getTasksCompleted()).append("\n");
        }

        return leaderboard.toString();
    }

    @NotNull
    @Override
    public String getChannelId() {
        return channelId;
    }

    @NotNull
    @Override
    public String getEmojID() {
        return emojID;
    }
}
