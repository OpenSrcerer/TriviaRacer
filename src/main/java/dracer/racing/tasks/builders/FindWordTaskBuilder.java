package dracer.racing.tasks.builders;

import dracer.racing.api.DictionaryAPI;
import dracer.racing.entities.WordDefPair;
import dracer.racing.tasks.Task;
import dracer.racing.tasks.TaskBuilder;
import dracer.racing.tasks.TaskImpl;

public class FindWordTaskBuilder implements TaskBuilder {
    private static final String incompleteQuestion = "Find the word that has the definition: ";

    public FindWordTaskBuilder() {
    }

    @Override
    public Task build() {
        WordDefPair pair = DictionaryAPI.getWordWithDef();
        String question = incompleteQuestion.concat("\"" + pair.def + "\"");
        return new TaskImpl(question, pair.word);
    }
}
