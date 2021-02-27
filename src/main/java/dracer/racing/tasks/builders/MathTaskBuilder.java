package dracer.racing.tasks.builders;

import dracer.racing.tasks.Task;
import dracer.racing.tasks.TaskBuilder;
import dracer.racing.tasks.TaskImpl;

import java.util.concurrent.ThreadLocalRandom;

public class MathTaskBuilder implements TaskBuilder {
    private static final String incompleteQuestion = "How much is ";

    public MathTaskBuilder() {
    }

    @Override
    public Task build() {
        int num1 = ThreadLocalRandom.current().nextInt(20);
        int num2 = ThreadLocalRandom.current().nextInt(20);
        String question = incompleteQuestion.concat(num1 + "x" + num2 + "?");
        String answer = String.valueOf(num1 * num2);
        return new TaskImpl(question, answer);
    }
}
