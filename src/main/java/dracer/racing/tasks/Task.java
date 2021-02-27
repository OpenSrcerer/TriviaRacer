package dracer.racing.tasks;

import java.util.Set;

public interface Task {
    void completedBy(String racerId);

    Set<String> haveCompleted();

    String getQuestion();

    String getAnswer();
}
