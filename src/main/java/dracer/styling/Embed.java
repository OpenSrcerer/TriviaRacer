package dracer.styling;

import dracer.TRacer;
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
        STARTING, CANCELLED, FINISHED,
        TRIVIA_QUESTION, TRIVIA_QUESTION_ANSWERABLE, TRIVIA_QUESTION_AFTER,
        SCIENCE, ENTERTAINMENT, OTHER,
        HELP, VOTE
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
            case CANCELLED -> cancelled(race.getPlayers().get(0).member.getId());
            case TRIVIA_QUESTION -> triviaQuestion(race, false);
            case TRIVIA_QUESTION_ANSWERABLE -> triviaQuestion(race, true);
            case TRIVIA_QUESTION_AFTER -> triviaQuestionAfter(race);
            case FINISHED -> finished();
            case OTHER -> otherCategories();
            case SCIENCE -> scienceCategories();
            case ENTERTAINMENT -> entertainmentCategories();
            case HELP -> help();
            case VOTE -> vote();
        }
    }

    private void starting() {
        long secondsToStart = 20;

        RaceTime time = race.getTime();
        if (time.areTemporalsSet()) {
            secondsToStart = time.getSecondsToStartOfRace() + 1;
        }

        if (race.getCategory() == Task.TaskCategory.ALL_CATEGORIES) {
            setTitle("A wild trivia race appears! 🎺 Type `tcr.join` to join!");
        } else {
            setTitle("A wild trivia race appears! 🎺 Type `tcr.join` to join!");
            setDescription("Specific Category: __" + race.getCategory().name + "__");
        }

        StringBuilder playersList = new StringBuilder(); // Get current players
        race.getPlayers().forEach(racer -> playersList.append("<@").append(racer.member.getId()).append(">\n"));
        addField("<:tB:643422476705202205> Current Participants:", playersList.toString(), false); // Show current players
        addField("⏱ Time to start:", secondsToStart + " seconds", false);
        setFooter("EmojID: " + race.getEmojID()); // Show Race's ID
        setTimestamp(Instant.now());
    }

    private void cancelled(String userId) {
        setTitle("❌ Race was cancelled.");
        setDescription("The race started by <@" + userId + "> was cancelled. \nAnd I was already typing the answers out...");
    }

    private void triviaQuestion(TriviaRace race, boolean isAnswerable) {
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

        if (!isAnswerable) {
            setFooter("Get ready to answer!");
            setImage("https://raw.githubusercontent.com/OpenSrcerer/TriviaRacer/main/src/main/java/dracer/img/triviaquestiontimer.gif?token=ALCYYNTMPE3DKYC5XNSISHLAIX5ZA");
        } else {
            setImage("https://raw.githubusercontent.com/OpenSrcerer/TriviaRacer/main/src/main/java/dracer/img/fasttimer.gif?token=ALCYYNRQNBEGKXHDQ6JNS4DAIZFKY");
        }
    }

    private void triviaQuestionAfter(TriviaRace race) {
        Task currentTask = race.getTasks().get(race.getCurrentTask());
        Set<String> taskCompleters = currentTask.haveCompleted();

        setTitle("Answers for Question " + (race.getCurrentTask() + 1) + ": __" + currentTask.getQuestion() + "__");
        setDescription("► Correct Answer: __**" + currentTask.getCorrectAnswer() + "**__");

        if (taskCompleters.isEmpty()) {
            addField("Wow, nobody got it right. 😢", "Better luck next time!", false);
        } else if (taskCompleters.size() == 1 && race.getPlayers().size() == 1) {
            addField("Oh snap! Only one person got it right! 👍 Fantastic job.",
                    "<@" + taskCompleters.iterator().next() + "> Considering you're playing solo, how about you bring some friends?", false);
        } else if (taskCompleters.size() == 1 && race.getPlayers().size() > 1) {
            addField("It seems that one user beat you all.",
                    "Or perhaps <@" + taskCompleters.iterator().next() + "> just got lucky.", false);
        } else {
            StringBuilder players = new StringBuilder();
            for (String player : taskCompleters) {
                players.append("<@").append(player).append(">\n");
            }
            addField("Awesome! ⭐ You guys got the correct answer:", players.toString(), false);
        }

        setImage("https://raw.githubusercontent.com/OpenSrcerer/TriviaRacer/main/src/main/java/dracer/img/fasttimer.gif?token=ALCYYNRQNBEGKXHDQ6JNS4DAIZFKY");
    }

    private void finished() {
        setTitle("The race has finished. Final Standings:");
        setDescription(race.getLeaderboard());
        setFooter("EmojID: " + race.getEmojID());
        setTimestamp(Instant.now());
    }

    private void otherCategories() {
        setTitle("Other Categories:");
        setDescription(
                "ID: **" + Task.TaskCategory.ALL_CATEGORIES.ordinal() + "** → " + Task.TaskCategory.ALL_CATEGORIES.name + "\n" +
                "ID: **" + Task.TaskCategory.GENERAL_KNOWLEDGE.ordinal() + "** → " + Task.TaskCategory.GENERAL_KNOWLEDGE.name + "\n" +
                "ID: **" + Task.TaskCategory.MYTHOLOGY.ordinal() + "** → " + Task.TaskCategory.MYTHOLOGY.name + "\n" +
                "ID: **" + Task.TaskCategory.SPORTS.ordinal() + "** → " + Task.TaskCategory.SPORTS.name + "\n" +
                "ID: **" + Task.TaskCategory.GEOGRAPHY.ordinal() + "** → " + Task.TaskCategory.GEOGRAPHY.name + "\n" +
                "ID: **" + Task.TaskCategory.HISTORY.ordinal() + "** → " + Task.TaskCategory.HISTORY.name + "\n" +
                "ID: **" + Task.TaskCategory.POLITICS.ordinal() + "** → " + Task.TaskCategory.POLITICS.name + "\n" +
                "ID: **" + Task.TaskCategory.ART.ordinal() + "** → " + Task.TaskCategory.ART.name + "\n" +
                "ID: **" + Task.TaskCategory.CELEBRITIES.ordinal() + "** → " + Task.TaskCategory.CELEBRITIES.name + "\n" +
                "ID: **" + Task.TaskCategory.ANIMALS.ordinal() + "** → " + Task.TaskCategory.ANIMALS.name + "\n" +
                "ID: **" + Task.TaskCategory.VEHICLES.ordinal() + "** → " + Task.TaskCategory.VEHICLES.name + "\n"
        );
        setFooter("Use the ID to start a race! Example: tcr.start 3");
    }

    private void scienceCategories() {
        setTitle("Science Categories:");
        setDescription(
                "ID: **" + Task.TaskCategory.SCIENCE_AND_NATURE.ordinal() + "** → " + Task.TaskCategory.SCIENCE_AND_NATURE.name + "\n" +
                "ID: **" + Task.TaskCategory.SCIENCE_COMPUTERS.ordinal() + "** → " + Task.TaskCategory.SCIENCE_COMPUTERS.name + "\n" +
                "ID: **" + Task.TaskCategory.SCIENCE_MATH.ordinal() + "** → " + Task.TaskCategory.SCIENCE_MATH.name + "\n" +
                "ID: **" + Task.TaskCategory.SCIENCE_GADGETS.ordinal() + "** → " + Task.TaskCategory.SCIENCE_GADGETS.name + "\n"
        );
        setFooter("Use the ID to start a race! Example: tcr.start 3");
    }

    private void entertainmentCategories() {
        setTitle("Entertainment Categories:");
        setDescription(
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_BOOKS.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_BOOKS.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_FILM.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_FILM.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_MUSIC.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_MUSIC.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_MUSICALS_THEATRES.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_MUSICALS_THEATRES.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_TELEVISION.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_TELEVISION.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_VIDEO_GAMES.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_VIDEO_GAMES.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_BOARD_GAMES.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_BOARD_GAMES.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_COMICS.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_COMICS.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_ANIME_MANGA.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_ANIME_MANGA.name + "\n" +
                "ID: **" + Task.TaskCategory.ENTERTAINMENT_CARTOONS_ANIMATIONS.ordinal() + "** → " + Task.TaskCategory.ENTERTAINMENT_CARTOONS_ANIMATIONS.name + "\n"
        );
        setFooter("Use the ID to start a race! Example: tcr.start 3");
    }

    private void help() {
        setTitle("Help Menu - TriviaRacer", "https://github.com/opensrcerer/triviaracer");
        setDescription("<:tB:643422476705202205> Hello there! Thank you for having me in your server.\n" +
                "TriviaRacer is a bot used to have fun trivia matches with your friends. " +
                "Here are all my commands:");
        addField("tcr.start `[categoryid]`", "Start a trivia race. " +
                "Leave `categoryid` empty if you want questions from all categories.", true);
        addField("tcr.join", "Join a trivia race.", true);
        addField("tcr.leave", "Leave a trivia race.", true);

        addField("View my available trivia categories:",
                """
                **tcr.other** - General Categories
                **tcr.science** - Science Related Categories
                **tcr.entertainment** - Entertainment Categories
                """,false);

        addField("Developed & Maintained with 💖 by", "<@178603029115830282>", false);
        setFooter("🚀 Gateway Ping: " + TRacer.tRacerInst.getGatewayPing());
    }

    private void vote() {
        setTitle("Vote Menu - TriviaRacer");
        setDescription(
                """
                Your support helps keep me alive! One vote could make a HUGE difference.
                [@top.gg](https://top.gg/bot/700341788136833065/vote)
                [@discord.boats](https://discord.boats/bot/700341788136833065/vote)
                Thank you for voting. 💖 Have a great day.
                """
        );
    }
}
