package dracer.racing.tasks;

import dracer.DracerExec;
import dracer.racing.tasks.builders.FindWordTaskBuilder;
import dracer.racing.tasks.builders.MathTaskBuilder;
import dracer.racing.tasks.builders.PreparedTaskBuilder;
import dracer.racing.tasks.builders.SloganTaskBuilder;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TaskList extends ArrayList<TaskBuilder> {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final TaskList singleton = new TaskList();

    private TaskList() {
        InputStream taskFile = DracerExec.class.getClassLoader().getResourceAsStream("tasks.json");
        if (taskFile == null) {
            throw new NullPointerException("Could not find task file!");
        }

        // Dynamic Tasks
        add(new MathTaskBuilder());
        add(new FindWordTaskBuilder());

        DataObject json = DataObject.fromJson(taskFile);
        DataArray prpTasks = json.getArray("prptasks");
        // Prepared Tasks
        for (int i = 0; i < prpTasks.length(); ++i) {
            DataObject object = prpTasks.getObject(i);

            String question = object.getString("question");
            String answer = object.getString("answer");

            add(new PreparedTaskBuilder(question, answer));
        }

        DataArray slgTasks = json.getArray("slgtasks");
        // Slogan Tasks
        for (int i = 0; i < slgTasks.length(); ++i) {
            DataObject object = slgTasks.getObject(i);

            String question = object.getString("question");
            String answer = object.getString("answer");

            add(new SloganTaskBuilder(question, answer));
        }
    }

    public static List<Task> getTasks(int tasksToGet) {
        List<Task> taskList = new ArrayList<>();

        for (int i = 0; i < tasksToGet; ++i) {
            taskList.add(singleton.get(ThreadLocalRandom.current().nextInt(singleton.size())).build());
        }

        return taskList;
    }
}
