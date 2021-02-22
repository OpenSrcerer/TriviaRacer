package dracer.racing.words;

import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class CleanWord implements DictionaryWord {
    private final String word;
    private final List<String> definitions;

    public CleanWord(String word, List<String> definitions) {
        this.word = word;
        this.definitions = definitions;
    }

    @Nonnull
    @Override
    public String getWord() {
        return word;
    }

    @Nullable
    @Override
    public String getFirstDefinition() {
        return (definitions == null) ? null : definitions.get(0);
    }
}
