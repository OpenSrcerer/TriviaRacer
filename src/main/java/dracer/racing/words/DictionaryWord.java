package dracer.racing.words;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface DictionaryWord {
    @Nonnull String getWord();

    @Nullable String getMeaning();

    default boolean isClean() {
        return this instanceof CleanWord;
    }
}
