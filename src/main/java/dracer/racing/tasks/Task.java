package dracer.racing.tasks;

import java.util.Set;

public interface Task {
    enum TaskDifficulty {
        EASY, MEDIUM, HARD
    }

    enum TaskCategory {
        // General
        ALL_CATEGORIES(8, "All Categories"),
        GENERAL_KNOWLEDGE(9, "General Knowledge"),

        // Entertainment
        ENTERTAINMENT_BOOKS(10, "Entertainment: Books"),
        ENTERTAINMENT_FILM(11, "Entertainment: Film"),
        ENTERTAINMENT_MUSIC(12, "Entertainment: Music"),
        ENTERTAINMENT_MUSICALS_THEATRES(13, "Entertainment: Musicals & Theatres"),
        ENTERTAINMENT_TELEVISION(14, "Entertainment: Television"),
        ENTERTAINMENT_VIDEO_GAMES(15, "Entertainment: Video Games"),
        ENTERTAINMENT_BOARD_GAMES(16, "Entertainment: Board Games"),
        ENTERTAINMENT_COMICS(29, "Entertainment: Comics"),
        ENTERTAINMENT_ANIME_MANGA(31, "Entertainment: Japanese Anime & Manga"),
        ENTERTAINMENT_CARTOONS_ANIMATIONS(32, "Entertainment: Cartoon & Animations"),

        // Science
        SCIENCE_AND_NATURE(17, "Science & Nature"),
        SCIENCE_COMPUTERS(18, "Science: Computers"),
        SCIENCE_MATH(19, "Science: Mathematics"),
        SCIENCE_GADGETS(30, "Science: Gadgets"),

        // Other
        MYTHOLOGY(20, "Mythology"),
        SPORTS(21, "Sports"),
        GEOGRAPHY(22, "Geography"),
        HISTORY(23, "History"),
        POLITICS(24, "Politics"),
        ART(25, "Art"),
        CELEBRITIES(26, "Celebrities"),
        ANIMALS(27, "Animals"),
        VEHICLES(28, "Vehicles");


        public int apiId;
        public String name;

        TaskCategory(int apiId, String name) {
            this.apiId = apiId;
            this.name = name;
        }
    }

    default String emojiMapper(String emoji) {
        return switch (emoji) {
            case "\uD83C\uDDE6" -> "a)";
            case "\uD83C\uDDE7" -> "b)";
            case "\uD83C\uDDE8" -> "c)";
            case "\uD83C\uDDE9" -> "d)";
            case "☑️" -> "true";
            case "\uD83C\uDDFD" -> "false";
            default -> throw new IllegalArgumentException("Emoji was invalid");
        };
    }

    void completedBy(String racerId);

    boolean triedBy(String racerId);

    boolean isCorrect(String tentativeAnswer);

    Set<String> haveCompleted();

    TaskDifficulty getDifficulty();

    TaskCategory getCategory();

    String getQuestion();

    String getCorrectAnswer();
}
