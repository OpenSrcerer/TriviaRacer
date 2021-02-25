package dracer.racing;

import dracer.Dracer;
import dracer.racing.entities.Racer;
import dracer.styling.Embed;
import dracer.styling.ImageProcessor;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static dracer.styling.Embed.EmbedFactory;

public final class RaceHandler {
    // ChannelID, Race
    public static final Map<String, DictionaryRace> activeRaces = new HashMap<>();

    public static void incrementWordsForRacer(String channelId, String racerId) {
        DictionaryRace race = activeRaces.get(channelId);
        race.incrementWords(racerId);
    }

    public static boolean isChannelRaceMode(String channelId) {
        DictionaryRace race = activeRaces.get(channelId);
        if (race != null) {
            return race.getState() == DictionaryRace.RaceState.IN_PROGRESS;
        }
        return false;
    }

    /**
     * @return True if race could be added, false if not.
     */
    public static boolean addRace(Guild guild, TextChannel channel, Member startingMember) {
        if (!isRaceActive(channel.getId())) {
            DictionaryRace newRace = new DictionaryRaceImpl(guild.getId(), channel.getId(), startingMember);
            activeRaces.put(channel.getId(), newRace);
            startSequence(channel, newRace);
            return true;
        }
        return false;
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
        TextChannel channel = race.getMessage().getTextChannel();

        // Remove the race and exit
        if (race.isCancelled()) {
            removeRace(channel.getId());
            return;
        }

        RaceTime time = race.getTime();
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
            race.setState(DictionaryRace.RaceState.IN_PROGRESS);
            race.getMessage().delete().queue();

            InputStream meaningsImage;
            try {
                meaningsImage = ImageProcessor.getImage(race.getWords());
            } catch (IOException ex) {
                race.cancelFuture();
                channel.sendMessage("Race cancelled due to an error: " + ex.getMessage()).queue();
                return;
            }

            channel.sendMessage(getAllRacerLanes(race))
                    .embed(EmbedFactory(race, Embed.EmbedType.ONGOING))
                    .addFile(meaningsImage, "image.png")
                    .queue(race::setMessage);
        }, Dracer.GRACE_PERIOD, TimeUnit.SECONDS);

        // Schedule Leaderboard Updates
        for (int index = Dracer.RACE_LENGTH - 5; index > 0; index-=5) {
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
        race.getMessage().editMessage(EmbedFactory(race, Embed.EmbedType.FINISHSEQ)).queue();
        race.setState(DictionaryRace.RaceState.FINISHED);
        removeRace(race.getChannelId());
    }

    public static void removeRace(String channelId) {
        activeRaces.remove(channelId);
    }
}
