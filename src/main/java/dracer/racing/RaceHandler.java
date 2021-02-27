package dracer.racing;

import dracer.Dracer;
import dracer.racing.entities.Racer;
import dracer.racing.tasks.TaskList;
import dracer.styling.Embed;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dracer.styling.Embed.EmbedFactory;

public final class RaceHandler {
    // ChannelID, Race
    public static final Map<String, DictionaryRace> activeRaces = new HashMap<>();

    public static void evaluateAnswer(String channelId, String racerId, String answer) {
        DictionaryRace race = activeRaces.get(channelId);
        if (race.evalAnswer(racerId, answer)) {
            finishSequence(race);
        }
    }

    public static boolean isChannelRaceMode(String channelId) {
        DictionaryRace race = activeRaces.get(channelId);
        if (race != null) {
            return race.getState() == DictionaryRace.RaceState.IN_PROGRESS;
        }
        return false;
    }

    public static void addRace(TextChannel channel, Member startingMember) {
        if (!isRaceActive(channel.getId())) {
            DictionaryRace newRace = new DictionaryRaceImpl(channel.getId(), startingMember);
            activeRaces.put(channel.getId(), newRace);
            startSequence(channel, newRace);
        }
    }

    public static boolean isRaceActive(String channelId) {
        return activeRaces.containsKey(channelId);
    }

    @Nullable
    public static DictionaryRace addRacerToRace(String channelId, Member tentativeRacer) {
        // Externally null checked.
        DictionaryRace runningRace = activeRaces.get(channelId);
        if (!runningRace.addRacer(tentativeRacer)) {
            return null; // Racer already in race
        }
        return runningRace;
    }

    @Nullable
    public static DictionaryRace removeRacer(String channelId, String racerId) {
        // Externally null checked.
        DictionaryRace runningRace = activeRaces.get(channelId);
        if (!runningRace.removeRacer(racerId)) {
            return null; // Racer not in race.
        }
        return runningRace;
    }

    private static String getAllRacerLanes(DictionaryRace race) {
        StringBuilder builder = new StringBuilder();
        for (Racer r : race.getPlayers()) {
            builder.append("🚩 | ").append(r.lane.getLane()).append(" | <@").append(r.member.getId()).append(">\n");
        }
        return builder.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━").toString();
    }

    public static void startSequence(TextChannel channel, DictionaryRace race) {
        // Race Start Notification
        channel.sendMessage(getAllRacerLanes(race))
                .embed(EmbedFactory(race, Embed.EmbedType.STARTING))
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

    public static void refreshRaceMessage(DictionaryRace race) {
        race.getMessage().editMessage(RaceHandler.getAllRacerLanes(race))
                .embed(Embed.EmbedFactory(race, Embed.EmbedType.STARTING))
                .queue();
    }

    private static void doRacePrep(DictionaryRace race) {
        race.setTime(); // Set the temporals for this race

        // Remove the race and exit
        if (race.isCancelled()) {
            removeRace(race.getMessage().getTextChannel().getId());
            return;
        }

        RaceTime time = race.getTime();
        // Initialize Tasks
        Dracer.RACE_EXECUTOR.execute(() -> race.setTasks(TaskList.getTasks(10)));
        // Further notification messages
        Dracer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                time.getSecondsPreGrace(10), TimeUnit.SECONDS);
        Dracer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                time.getSecondsPreGrace(3), TimeUnit.SECONDS);
        Dracer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                time.getSecondsPreGrace(2), TimeUnit.SECONDS);
        Dracer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.STARTING)).queue(),
                time.getSecondsPreGrace(1), TimeUnit.SECONDS);

        // Start the race
        Dracer.RACE_EXECUTOR.schedule(() -> {
            try {
                race.setState(DictionaryRace.RaceState.IN_PROGRESS);
                race.getMessage().delete().queue();

                race.getMessage().getTextChannel().sendMessage(getAllRacerLanes(race))
                        .embed(EmbedFactory(race, Embed.EmbedType.ONGOING))
                        .queue(race::setMessage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, Dracer.GRACE_PERIOD, TimeUnit.SECONDS);

        // Schedule Leaderboard Updates
        for (int index = Dracer.RACE_LENGTH - 5; index > 0; index -= 5) {
            Dracer.RACE_EXECUTOR.schedule(() -> race.getMessage().editMessage(getAllRacerLanes(race))
                    .embed(EmbedFactory(race, Embed.EmbedType.ONGOING))
                    .queue(), time.getSecondsPreEndOfRace(index), TimeUnit.SECONDS);
        }

        // Schedule Race End
        race.setEndFuture(Dracer.RACE_EXECUTOR.schedule(() -> {
            finishSequence(race);
            return null;
        }, Dracer.TOTAL_LENGTH, TimeUnit.SECONDS));
    }

    public static void finishSequence(DictionaryRace race) {
        race.getMessage().delete().queue();
        race.getMessage().getTextChannel().sendMessage(EmbedFactory(race, Embed.EmbedType.FINISHSEQ)).queue();
        race.setState(DictionaryRace.RaceState.FINISHED);
        removeRace(race.getChannelId());
    }

    public static void removeRace(String channelId) {
        activeRaces.remove(channelId);
    }
}
