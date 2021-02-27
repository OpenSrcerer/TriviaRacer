package dracer.racing.tasks;

import java.util.HashSet;
import java.util.Set;

public class TaskImpl implements Task {
    private final Set<String> racersCompleted = new HashSet<>();
    private final String question;
    private final String answer;

    public TaskImpl(String q, String a) {
        question = q;
        answer = a;
    }

    @Override
    public void completedBy(String racerId) {
        racersCompleted.add(racerId);
    }

    @Override
    public Set<String> haveCompleted() {
        return racersCompleted;
    }

    @Override
    public String getQuestion() {
        return question;
    }

    @Override
    public String getAnswer() {
        return answer;
    }
}
