package dracer.racing;

import dracer.TRacer;
import dracer.racing.api.TriviaAPI;
import dracer.racing.entities.Racer;
import dracer.racing.tasks.MultipleChoiceTask;
import dracer.racing.tasks.Task;
import dracer.styling.Embed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static dracer.styling.Embed.EmbedFactory;

public final class RaceHandler {
    private static final Logger lgr = LoggerFactory.getLogger(RaceHandler.class);
    // ChannelID, Race
    public static final Map<String, TriviaRace> activeRaces = new HashMap<>();

    public static boolean evaluateAnswer(String channelId, String racerId, String emoji) {
        TriviaRace race = activeRaces.get(channelId);
        return race.evalAnswer(racerId, emoji);
    }

    public static boolean isChannelRaceMode(String channelId) {
        TriviaRace race = activeRaces.get(channelId);
        if (race != null) {
            return race.getState() == TriviaRace.RaceState.IN_PROGRESS;
        }
        return false;
    }

    public static void addRace(Task.TaskCategory category, TextChannel channel, Member startingMember) {
        if (!findRace(channel.getId())) {
            TriviaRace newRace = new TriviaRaceImpl(category, channel.getId(), startingMember);
            activeRaces.put(channel.getId(), newRace);
            startSequence(channel, newRace);
            lgr.info("Created new race for category " + newRace.getCategory().name + " with ID:" + newRace.getEmojID());
        }
    }

    public static boolean findRace(String channelId) {
        return activeRaces.containsKey(channelId);
    }

    public static TriviaRace getRace(String channelId) {
        return activeRaces.get(channelId);
    }

    public static void addRacer(String channelId, Member tentativeRacer) {
        // Externally null checked.
        TriviaRace runningRace = activeRaces.get(channelId);
        runningRace.addRacer(tentativeRacer);
    }

    public static void removeRacer(String channelId, String racerId) {
        // Externally null checked.
        TriviaRace runningRace = activeRaces.get(channelId);
        runningRace.removeRacer(racerId);
    }

    private static String getAllRacerLanes(TriviaRace race) {
        StringBuilder builder = new StringBuilder();
        for (Racer r : race.getPlayers()) {
            builder.append(r.lane.getLane()).append(" [<@").append(r.member.getId()).append(">]\n");
        }
        return builder.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").toString();
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
                    race.cancel(); // Cancel the race
                    return channel.sendMessage("Race cancelled due to an error: " + throwable.getMessage());
                }).queue();
    }

    public static void refreshRaceMessage(TriviaRace race) {
        race.getMessage().editMessage(Embed.EmbedFactory(race, Embed.EmbedType.STARTING)).queue();
    }

    private static void doRacePrep(TriviaRace race) {
        race.setTime(); // Set the temporals for this race
        Future<List<Task>> taskFuture = TriviaAPI.requestTasks(race.getCategory());
        List<ScheduledFuture<?>> futures = new ArrayList<>();

        // Remove the race and exit
        if (race.isCancelled()) {
            removeRace(race.getMessage().getTextChannel().getId());
            return;
        }

        // Further notification messages
        futures.addAll(raceStartNotification(race));
        futures.addAll(Arrays.asList(raceStart(race, taskFuture), finishSequence(race)));
        futures.addAll(scheduleTriviaQuestions(race));
        race.addActions(futures);
    }

    private static List<ScheduledFuture<?>> raceStartNotification(TriviaRace race) {
        return Arrays.asList(
                TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                        race.getTime().getSecondsPreGrace(10), TimeUnit.SECONDS),
                TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                        race.getTime().getSecondsPreGrace(3), TimeUnit.SECONDS),
                TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                        race.getTime().getSecondsPreGrace(2), TimeUnit.SECONDS),
                TRacer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                        race.getTime().getSecondsPreGrace(1), TimeUnit.SECONDS)
        );
    }

    private static ScheduledFuture<?> raceStart(TriviaRace race, Future<List<Task>> taskFuture) {
        // Start the race
        return TRacer.RACE_EXECUTOR.schedule(() -> {
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

    private static List<ScheduledFuture<?>> scheduleTriviaQuestions(TriviaRace race) {
        List<ScheduledFuture<?>> futures = new ArrayList<>();

        // Schedule Trivia Questions
        int secondsPrior = TRacer.RACE_LENGTH;

        for (int task = 0; task < TRacer.TASK_COUNT; ++task) {
            futures.add(TRacer.RACE_EXECUTOR.schedule(() -> {
                race.getMessage().delete().queue();
                race.incrementCurrentTask();
                race.getChannel().sendMessage(getAllRacerLanes(race))
                        .embed(EmbedFactory(race, Embed.EmbedType.TRIVIA_QUESTION))
                        .queue(race::setMessage);
            }, race.getTime().getSecondsPreEndOfRace(secondsPrior), TimeUnit.SECONDS));

            futures.add(TRacer.RACE_EXECUTOR.schedule(() ->
                    race.getMessage().editMessage(getAllRacerLanes(race))
                            .embed(EmbedFactory(race, Embed.EmbedType.TRIVIA_QUESTION_ANSWERABLE))
                            .flatMap(message -> addReactions(race.getTasks().get(race.getCurrentTask()), message)).queue(),
                    race.getTime().getSecondsPreEndOfRace(secondsPrior - TRacer.READING_TIME), TimeUnit.SECONDS));

            futures.add(TRacer.RACE_EXECUTOR.schedule(() ->
                    race.getMessage().editMessage(getAllRacerLanes(race))
                            .embed(EmbedFactory(race, Embed.EmbedType.TRIVIA_QUESTION_AFTER)).queue(),
                    race.getTime().getSecondsPreEndOfRace(secondsPrior - TRacer.READING_TIME + TRacer.ANSWER_TIME), TimeUnit.SECONDS));

            secondsPrior -= TRacer.TRIVIA_QUESTION_TOTAL;
        }

        return futures;
    }

    private static RestAction<Void> addReactions(Task task, Message message) {
        if (task instanceof MultipleChoiceTask) {
            return message.addReaction("\uD83C\uDDE6")
                    .and(message.addReaction("\uD83C\uDDE7"))
                    .and(message.addReaction("\uD83C\uDDE8"))
                    .and(message.addReaction("\uD83C\uDDE9"));
        } else {
            return message.addReaction("☑️")
                    .and(message.addReaction("\uD83C\uDDFD"));
        }
    }

    private static ScheduledFuture<?> finishSequence(TriviaRace race) {
        // Schedule Race End
        return TRacer.RACE_EXECUTOR.schedule(() -> {
            race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.FINISHED)).queue();
            race.setState(TriviaRace.RaceState.FINISHED);
            removeRace(race.getChannelId());
            lgr.info("Finished race with ID:" + race.getEmojID());
            return null;
        }, TRacer.TOTAL_LENGTH, TimeUnit.SECONDS);
    }

    public static void removeRace(String channelId) {
        activeRaces.remove(channelId);
    }
}
