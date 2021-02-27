package dracer.racing.tasks.builders;

import dracer.racing.tasks.Task;
import dracer.racing.tasks.TaskBuilder;
import dracer.racing.tasks.TaskImpl;

public class SloganTaskBuilder implements TaskBuilder {
    private final String question;
    private final String answer;

    public SloganTaskBuilder(String q, String a) {
        question = "Which is the company or franchise that uses this slogan: \"" + q + "\"?";
        answer = a;
    }

    @Override
    public Task build() {
        return new TaskImpl(question, answer);
    }
}
