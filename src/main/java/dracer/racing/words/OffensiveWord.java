package dracer.racing.words;

import dracer.Dracer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

public class OffensiveWord implements DictionaryWord {
    private final String word;

    public OffensiveWord() {
        /*if (clean) {
            word = Dracer.cleanWords
        } else {
            word = Dracer.cleanWords
        }*/

        word = Dracer.cleanWords.get(ThreadLocalRandom.current().nextInt(Dracer.cleanWords.size()));

    }

    @Override
    @Nonnull
    public String getWord() {
        return word;
    }

    @Override
    @Nullable
    public String getFirstDefinition() {
        return null;
    }
}
