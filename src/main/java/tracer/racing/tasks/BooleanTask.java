package tracer.racing.tasks;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BooleanTask implements Task {
    private final Set<String> answeredCorrectly = new HashSet<>();
    private final Set<String> tried = new HashSet<>();
    private final TaskDifficulty difficulty;
    private final TaskCategory category;
    private final String question;
    private final String answer;

    public BooleanTask(TaskCategory c, TaskDifficulty d, String q, String a) {
        category = c;
        difficulty = d;
        question = q;
        answer = a;
    }

    @Override
    public void completedBy(String racerId) {
        answeredCorrectly.add(racerId);
    }

    @Override
    public boolean triedBy(String racerId) {
        return tried.add(racerId);
    }

    @Override
    public boolean isCorrect(String answerEmoji) {
        answerEmoji = emojiMapper(answerEmoji);
        String correctAnswer = answer.toLowerCase(Locale.ROOT).trim();
        return answerEmoji.equals(correctAnswer);
    }

    @Override
    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    @Override
    public TaskCategory getCategory() {
        return category;
    }

    @Override
    public Set<String> haveCompleted() {
        return answeredCorrectly;
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public String getCorrectAnswer() {
        return answer;
    }
}
