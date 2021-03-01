package dracer.styling;

import dracer.racing.TriviaRace;
import dracer.racing.tasks.MultipleChoiceTask;
import dracer.racing.tasks.Task;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class Embed extends EmbedBuilder {
    public enum EmbedType {
        STARTING, TRIVIA_QUESTION, TRIVIA_QUESTION_AFTER, FINISHSEQ
    }

    /**
     * Default color for all Embeds.
     */
    private static final int embedColor = 0x2f3136;

    /**
     * Race that the Embed refers to.
     */
    private final TriviaRace race;
    public static MessageEmbed EmbedFactory(TriviaRace race, EmbedType type) {
        return new Embed(race, type).build();
    }

    private Embed(TriviaRace race, EmbedType type) {
        super();
        this.race = race;
        setColor(embedColor);

        switch (type) {
            case STARTING -> starting();
            case TRIVIA_QUESTION -> triviaQuestion(race);
            case TRIVIA_QUESTION_AFTER -> triviaQuestionAfter(race);
            case FINISHSEQ -> finished();
        }
    }

    private void starting() {
        long secondsToStart = 20;

        RaceTime time = race.getTime();
        if (time.isTemporalsSet()) {
            secondsToStart = time.getSecondsToStartOfRace() + 1;
        }

        setTitle("A wild trivia race appears! Type `tcr.join` to join!");
        StringBuilder playersList = new StringBuilder(); // Get current players
        race.getPlayers().forEach(racer -> playersList.append("<@").append(racer.member.getId()).append(">\n"));
        addField("Current Participants:", playersList.toString(), false); // Show current players
        addField("Time to start:", secondsToStart + " seconds", false);
        setFooter("EmojID: " + race.getEmojID()); // Show Race's ID
        setTimestamp(Instant.now());
    }

    private void triviaQuestion(TriviaRace race) {
        Task currentTask = race.getTasks().get(race.getCurrentTask());
        String taskType = (currentTask instanceof MultipleChoiceTask) ? "Multiple Choice" : "True or False";

        setTitle("Question " + (race.getCurrentTask() + 1) + ": __" + currentTask.getQuestion() + "__");
        setDescription("🧨 Type: *" + taskType + "*\n👓 Category: *" + currentTask.getCategory().name + "*\n🔥 Difficulty: *"
        + currentTask.getDifficulty().toString() + "*");

        if (taskType.equals("Multiple Choice")) {
            Map<String, String> answerMap = ((MultipleChoiceTask) currentTask).getAnswerMap();
            String reply = "a) **" + answerMap.get("a)") + "**\nb) **" + answerMap.get("b)") +
                    "**\nc) **" + answerMap.get("c)") + "**\nd) **" + answerMap.get("d)") + "**";
            addField("Answer with:", reply, false);
        } else {
            addField("Answer with:", "a) **True**\nb) **False**", false);
        }
    }

    private void triviaQuestionAfter(TriviaRace race) {
        Task currentTask = race.getTasks().get(race.getCurrentTask());
        Set<String> taskCompleters = currentTask.haveCompleted();

        setTitle("Answers for Question " + (race.getCurrentTask() + 1) + ": " + currentTask.getQuestion());
        setDescription("Correct Answer: " + currentTask.getCorrectAnswer());

        if (taskCompleters.isEmpty()) {
            addField("Wow, nobody got it right. 😢", "Better luck next time!", false);
        } else {
            StringBuilder players = new StringBuilder();
            for (String player : taskCompleters) {
                players.append("<@").append(player).append(">\n");
            }
            addField("Awesome! ⭐ You guys got the correct answer:", players.toString(), false);
        }
    }

    private void finished() {
        setTitle("The race has finished. Final Standings:");
        setDescription(race.getLeaderboard());
        setFooter("EmojID: " + race.getEmojID());
        setTimestamp(Instant.now());
    }
}
