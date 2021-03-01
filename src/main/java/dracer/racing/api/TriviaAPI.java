package dracer.racing.api;

import dracer.TRacer;
import dracer.racing.tasks.BooleanTask;
import dracer.racing.tasks.MultipleChoiceTask;
import dracer.racing.tasks.Task;
import dracer.styling.HtmlEntities;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public abstract class TriviaAPI {
    private static final Logger lgr = LoggerFactory.getLogger(TriviaAPI.class);
    private static final OkHttpClient client = new OkHttpClient();

    // API URLs
    private static final String API_URL = "https://opentdb.com/api.php?amount=" + TRacer.TASK_COUNT + "&category=@";
    private static final String API_URL_NOCAT = "https://opentdb.com/api.php?amount=" + TRacer.TASK_COUNT;

    private static HttpUrl buildUrl(int category) {

        final HttpUrl url;
        if (category == 8) {
            url = HttpUrl.parse(API_URL_NOCAT);
        } else {
            url = HttpUrl.parse(API_URL.replaceFirst("@", String.valueOf(category)));
        }

        if (url == null) {
            throw new IllegalArgumentException();
        }

        return url.newBuilder().build();
    }

    public static Future<List<Task>> requestTasks(Task.TaskCategory category) {
        HttpUrl url = buildUrl(category.apiId);
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Queue the async request
        return TRacer.RACE_EXECUTOR.submit(() -> {
            try (Response response = client.newCall(request).execute()) {
                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        lgr.warn("Trivia API body was null.");
                        return null;
                    }
                    return parseResponse(DataObject.fromJson(body.string()));
                }
            } catch (IOException e) {
                lgr.warn("Error while retrieving Trivia questions!", e);
                return null;
            }
        });
    }

    public static List<Task> parseResponse(DataObject json) {
        List<Task> tasks = new ArrayList<>();

        DataArray results = json.getArray("results");
        for (int i = 0; i < results.length(); ++i) {
            DataObject triviaTask = results.getObject(i);
            final String questionType = triviaTask.getString("type");

            final Task.TaskCategory category = getTaskCategory(triviaTask.getString("category"));
            final Task.TaskDifficulty difficulty = getDifficulty(triviaTask.getString("difficulty"));
            final String question = HtmlEntities.decode(triviaTask.getString("question"));

            if (questionType.equals("multiple")) {
                final List<String> wrongAnswersList = new ArrayList<>();
                final String correctAnswer = HtmlEntities.decode(triviaTask.getString("correct_answer"));
                final DataArray wrongAnswers = triviaTask.getArray("incorrect_answers");

                for (int j = 0; j < wrongAnswers.length(); ++j) {
                    wrongAnswersList.add(HtmlEntities.decode(wrongAnswers.getString(j)));
                }
                tasks.add(new MultipleChoiceTask(category, difficulty, question, correctAnswer, wrongAnswersList));
            } else {
                tasks.add(new BooleanTask(category, difficulty, question, triviaTask.getString("correct_answer")));
            }
        }

        return Collections.unmodifiableList(tasks);
    }

    private static Task.TaskCategory getTaskCategory(String category) {
        for (Task.TaskCategory taskCategory : Task.TaskCategory.class.getEnumConstants()) {
            if (taskCategory.name.equals(category)) {
                return taskCategory;
            }
        }
        System.out.println(category);
        throw new IllegalArgumentException("Category could not be matched!");
    }

    private static Task.TaskDifficulty getDifficulty(String difficulty) {
        return switch (difficulty) {
            case "easy" -> Task.TaskDifficulty.EASY;
            case "medium" -> Task.TaskDifficulty.MEDIUM;
            case "hard" -> Task.TaskDifficulty.HARD;
            default -> throw new IllegalArgumentException("Difficulty could not be matched!");
        };
    }
}
