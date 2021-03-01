package dracer.racing;

import dracer.TRacer;
import dracer.racing.api.TriviaAPI;
import dracer.racing.entities.Racer;
import dracer.racing.tasks.Task;
import dracer.styling.Embed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static dracer.styling.Embed.EmbedFactory;

public final class RaceHandler {
    private static final Logger lgr = LoggerFactory.getLogger(RaceHandler.class);
    // ChannelID, Race
    public static final Map<String, TriviaRace> activeRaces = new HashMap<>();

    public static boolean evaluateAnswer(String channelId, String racerId, String answer) {
        TriviaRace race = activeRaces.get(channelId);
        return race.evalAnswer(racerId, answer);
    }

    public static boolean isChannelRaceMode(String channelId) {
        TriviaRace race = activeRaces.get(channelId);
        if (race != null) {
            return race.getState() == TriviaRace.RaceState.IN_PROGRESS;
        }
        return false;
    }

    public static void addRace(Task.TaskCategory category, TextChannel channel, Member startingMember) {
        if (!isRaceActive(channel.getId())) {
            TriviaRace newRace = new TriviaRaceImpl(category, channel.getId(), startingMember);
            activeRaces.put(channel.getId(), newRace);
            startSequence(channel, newRace);
            lgr.info("Created new race for category " + newRace.getCategory().name + " with ID:" + newRace.getEmojID());
        }
    }

    public static boolean isRaceActive(String channelId) {
        return activeRaces.containsKey(channelId);
    }

    @Nullable
    public static TriviaRace addRacerToRace(String channelId, Member tentativeRacer) {
        // Externally null checked.
        TriviaRace runningRace = activeRaces.get(channelId);
        if (!runningRace.addRacer(tentativeRacer)) {
            return null; // Racer already in race
        }
        return runningRace;
    }

    @Nullable
    public static TriviaRace removeRacer(String channelId, String racerId) {
        // Externally null checked.
        TriviaRace runningRace = activeRaces.get(channelId);
        if (!runningRace.removeRacer(racerId)) {
            return null; // Racer not in race.
        }
        return runningRace;
    }

    private static String getAllRacerLanes(TriviaRace race) {
        StringBuilder builder = new StringBuilder();
        for (Racer r : race.getPlayers()) {
            builder.append("|").append(r.lane.getLane()).append(" | <@")
                    .append(r.member.getId()).append(">\n").append("(").append(r.getTasksCompleted()).append("/10)");
        }
        return builder.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").toString();
    }

    public static void startSequence(TextChannel channel, TriviaRace race) {
        // Race Start Notification
        channel.sendMessage(EmbedFactory(race, Embed.EmbedType.STARTING))
                .map(message -> {
                    race.setMessage(message);
                    doRacePrep(race);
                    return message;
                })
                .onErrorFlatMap(throwable -> {
                    race.cancelFuture(); // Cancel the race
                    return channel.sendMessage("Race cancelled due to an error: " + throwable.getMessage());
                }).queue();
    }

    public static void refreshRaceMessage(TriviaRace race) {
        race.getMessage().editMessage(RaceHandler.getAllRacerLanes(race))
                .embed(Embed.EmbedFactory(race, Embed.EmbedType.STARTING))
                .queue();
    }

    private static void doRacePrep(TriviaRace race) {
        race.setTime(); // Set the temporals for this race
        Future<List<Task>> taskFuture = TriviaAPI.requestTasks(race.getCategory());

        // Remove the race and exit
        if (race.isCancelled()) {
            removeRace(race.getMessage().getTextChannel().getId());
            return;
        }

        // Further notification messages
        TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                race.getTime().getSecondsPreGrace(10), TimeUnit.SECONDS);
        TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                race.getTime().getSecondsPreGrace(3), TimeUnit.SECONDS);
        TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                race.getTime().getSecondsPreGrace(2), TimeUnit.SECONDS);
        TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                race.getTime().getSecondsPreGrace(1), TimeUnit.SECONDS);

        raceStart(race, taskFuture);
        scheduleTriviaQuestions(race);
        finishSequence(race);
    }

    private static void raceStart(TriviaRace race, Future<List<Task>> taskFuture) {
        // Start the race
        TRacer.RACE_EXECUTOR.schedule(() -> {
            try {
                race.setTasks(taskFuture.get(1, TimeUnit.SECONDS));
                race.setState(TriviaRace.RaceState.IN_PROGRESS);
            } catch (Exception ex) {
                lgr.error("Error:", ex);
                race.getChannel().sendMessage("Race was unable to start due to an API problem. Please try again later.").queue();
                removeRace(race.getChannelId());
            }
        }, TRacer.GRACE_PERIOD, TimeUnit.SECONDS);
    }

    private static void scheduleTriviaQuestions(TriviaRace race) {
        // Schedule Trivia Questions
        int secondsPrior = TRacer.RACE_LENGTH;

        for (int task = 0; task < TRacer.TASK_COUNT; ++task) {
            TRacer.RACE_EXECUTOR.schedule(() -> {
                race.getMessage().delete().queue();
                race.incrementCurrentTask();
                race.getChannel().sendMessage(getAllRacerLanes(race))
                        .embed(EmbedFactory(race, Embed.EmbedType.TRIVIA_QUESTION))
                        .queue(race::setMessage);
                }, race.getTime().getSecondsPreEndOfRace(secondsPrior), TimeUnit.SECONDS);

            TRacer.RACE_EXECUTOR.schedule(() -> {
                race.getMessage().delete().queue();
                race.getChannel().sendMessage(getAllRacerLanes(race))
                        .embed(EmbedFactory(race, Embed.EmbedType.TRIVIA_QUESTION_AFTER))
                        .queue(race::setMessage);
                }, race.getTime().getSecondsPreEndOfRace(secondsPrior - 15), TimeUnit.SECONDS);

            secondsPrior -= 20;
        }
    }

    private static void finishSequence(TriviaRace race) {
        // Schedule Race End
        race.setEndFuture(TRacer.RACE_EXECUTOR.schedule(() -> {
            race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.FINISHSEQ)).queue();
            race.setState(TriviaRace.RaceState.FINISHED);
            removeRace(race.getChannelId());
            lgr.info("Finished race with ID:" + race.getEmojID());
            return null;
        }, TRacer.TOTAL_LENGTH, TimeUnit.SECONDS));
    }

    public static void removeRace(String channelId) {
        activeRaces.remove(channelId);
    }
}
