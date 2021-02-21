package dracer.racing.words;

import dracer.Dracer;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadLocalRandom;

public class CleanWord implements DictionaryWord {
    private final String word;
    private final String meaning;

    protected CleanWord() {
        word = Dracer.cleanWords.get(ThreadLocalRandom.current().nextInt(Dracer.cleanWords.size()));
    }

    @Nonnull
    @Override
    public String getWord() {
        return word;
    }

    @Nullable
    @Override
    public String getMeaning() {
        return null;
    }
}
