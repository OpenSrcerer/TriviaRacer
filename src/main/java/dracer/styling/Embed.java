package dracer.styling;

import dracer.racing.DictionaryRace;
import dracer.racing.tasks.Task;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.List;

public class Embed extends EmbedBuilder {
    public enum EmbedType {
        STARTING, ONGOING, FINISHSEQ
    }

    /**
     * Default color for all Embeds.
     */
    private static final int embedColor = 0x2f3136;

    private final DictionaryRace race;

    public static MessageEmbed EmbedFactory(DictionaryRace race, EmbedType type) {
        return new Embed(race, type).build();
    }

    private Embed(DictionaryRace race, EmbedType type) {
        super();
        this.race = race;
        setColor(embedColor);

        switch (type) {
            case STARTING -> starting();
            case ONGOING -> ongoingRaceStats(race.getTasks());
            case FINISHSEQ -> finished(race.getTasks());
        }
    }

    private void starting() {
        long secondsToStart = 20;

        RaceTime time = race.getTime();
        if (time.isTemporalsSet()) {
            secondsToStart = time.getSecondsToStartOfRace() + 1;
        }

        setTitle("A wild dictionary race appears! Type `dcr.join` to join!");
        StringBuilder playersList = new StringBuilder(); // Get current players
        race.getPlayers().forEach(racer -> playersList.append("<@").append(racer.member.getId()).append(">\n"));
        addField("Current Participants:", playersList.toString(), false); // Show current players
        addField("Time to start:", secondsToStart + " seconds", false);
        setFooter("EmojID: " + race.getEmojID()); // Show Race's ID
        setTimestamp(Instant.now());
    }

    private void ongoingRaceStats(List<Task> tasks) {
        setTitle("The race is in progress:");
        setDescription(race.getLeaderboard());

        for (Task t : tasks) {
            addField(t.getQuestion(), "", false);
        }

        setFooter("EmojID: " + race.getEmojID());
        setTimestamp(Instant.now());
    }

    private void finished(List<Task> tasks) {
        setTitle("The race has finished. Final Standings:");
        setDescription(race.getLeaderboard());

        for (Task t : tasks) {
            addField(t.getQuestion(), "Answer: " + t.getAnswer(), false);
        }

        setFooter("EmojID: " + race.getEmojID());
        setTimestamp(Instant.now());
    }
}
