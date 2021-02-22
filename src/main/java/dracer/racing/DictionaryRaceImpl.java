package dracer.racing;

import dracer.racing.api.DictionaryAPI;
import dracer.racing.words.CleanWord;
import dracer.racing.words.DictionaryWord;
import net.dv8tion.jda.api.entities.Member;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class DictionaryRaceImpl implements DictionaryRace, Callback {
    private final Map<String, Racer> players = new HashMap<>();
    private final List<DictionaryWord> raceWords = Collections.synchronizedList(new ArrayList<>());

    private RaceState state = RaceState.STARTING;
    private Racer winner;

    protected DictionaryRaceImpl(Member startingMember) {
        DictionaryAPI.getWords(this,10);
        players.put(startingMember.getId(), new Racer(startingMember));
    }

    // ---- Callback Overrides ----
    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String word = call.request().header("word");

        List<String> definitions = new ArrayList<>();
        if (!response.isSuccessful()) {
            definitions.add("No definition found for this one :(");
            raceWords.add(new CleanWord(word, definitions));
        } else {
            definitions = DictionaryAPI.parseResponse(response);
            raceWords.add(new CleanWord(word, definitions));
        }
        System.out.println("Callback " + raceWords.size() + "!");
        if (raceWords.size() == 10) {
            raceWords.forEach(w -> System.out.println(w.getWord() + " -> " + w.getFirstDefinition()));
        }
    }

    // ---- Command Overrides ----
    @Override
    public void joinStartingGame(Member member) {
        players.put(member.getId(), new Racer(member));
    }

    @Override
    public void setState(RaceState state) {
        this.state = state;
    }

    @Override
    public void setWinner(String userId) {
        winner = players.get(userId);
    }

    @NotNull
    @Override
    public List<DictionaryWord> getWords() {
        return raceWords;
    }

    @NotNull
    @Override
    public List<Racer> getPlayers() {
        return new ArrayList<>(players.values());
    }

    @NotNull
    @Override
    public RaceState getState() {
        return state;
    }

    @Nullable
    @Override
    public Racer getWinner() {
        return winner;
    }
}
