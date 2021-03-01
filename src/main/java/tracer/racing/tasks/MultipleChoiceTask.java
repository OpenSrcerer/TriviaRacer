package tracer.racing.tasks;

import java.util.*;

public class MultipleChoiceTask implements Task {
    private final Set<String> answeredCorrectly = new HashSet<>();
    private final Set<String> tried = new HashSet<>();
    private final List<String> wrongAnswers;
    private final TaskDifficulty difficulty;
    private final TaskCategory category;
    private final String question;
    private final String correctAnswer;

    private Map<String, String> answerMap;

    public MultipleChoiceTask(TaskCategory c, TaskDifficulty d, String q, String ca, List<String> a) {
        category = c;
        difficulty = d;
        question = q;
        correctAnswer = ca;
        wrongAnswers = a;
        setAnswerMap();
    }

    public void setAnswerMap() {
        Map<String, String> answerMap = new HashMap<>();
        List<String> reorderedAnswers = new ArrayList<>(wrongAnswers);
        reorderedAnswers.add(correctAnswer);
        Collections.shuffle(reorderedAnswers);

        for (int i = 0; i < reorderedAnswers.size(); ++i) {
            String letter;
            switch (i) {
                case 0 -> letter = "a)";
                case 1 -> letter = "b)";
                case 2 -> letter = "c)";
                case 3 -> letter = "d)";
                default -> letter = "x)";
            }
            answerMap.put(letter, reorderedAnswers.get(i));
        }
        this.answerMap = answerMap;
    }

    public Map<String, String> getAnswerMap() {
        return answerMap;
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
        String cAnswer = correctAnswer.toLowerCase(Locale.ROOT).trim();

        String answerFromMap = answerMap.get(answerEmoji);
        if (answerFromMap != null) {
            answerFromMap = answerFromMap.toLowerCase(Locale.ROOT).trim();
            return answerFromMap.equals(cAnswer);
        }
        return false;
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
        return correctAnswer;
    }
}
