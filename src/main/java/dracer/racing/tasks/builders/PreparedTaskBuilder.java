package dracer.racing.tasks.builders;

import dracer.racing.tasks.Task;
import dracer.racing.tasks.TaskBuilder;
import dracer.racing.tasks.TaskImpl;

public class PreparedTaskBuilder implements TaskBuilder {
    private final String question;
    private final String answer;

    public PreparedTaskBuilder(String q, String a) {
        question = q;
        answer = a;
    }

    @Override
    public Task build() {
        return new TaskImpl(question, answer);
    }
}
