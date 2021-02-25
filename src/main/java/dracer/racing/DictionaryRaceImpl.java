package dracer.racing;

import dracer.Dracer;
import dracer.racing.api.DictionaryAPI;
import dracer.racing.entities.Racer;
import dracer.racing.words.CleanWord;
import dracer.racing.words.DictionaryWord;
import dracer.util.RaceTime;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import okhttp3.Call;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public final class DictionaryRaceImpl implements DictionaryRace {

    // --- Immutable ---
    private final Map<String, Racer> players = new HashMap<>();
    private final List<DictionaryWord> raceWords = Collections.synchronizedList(new ArrayList<>());
    private final RaceTime time = new RaceTime(); // Holds the race's length and other timeframes where actions needs to be taken
    private final String guildId, channelId;
    private final String emojID = Dracer.getRandomEmojis();
    // -----------------

    // --- Mutable ---
    private ScheduledFuture<Void> raceEndFuture; // A ScheduledFuture that represents when finishSequence() is called.
    private RaceState state = RaceState.STARTING; // Race's current state
    private Message message; // Message to refer to and update while racing
    private boolean cancelled = false;
    // -----------------

    protected DictionaryRaceImpl(String guildId, String channelId, Member startingMember) {
        this.guildId = guildId;
        this.channelId = channelId;
        DictionaryAPI.getWords(this,10);
        players.put(startingMember.getId(), new Racer(startingMember));
    }

    // ---- Callback Overrides ----
    @Override
    public void onFailure(@NotNull Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        String word = call.request().header("word");
        List<String> definitions = new ArrayList<>();

        if (!response.isSuccessful()) {
            definitions.add("No definition found for this one :(");
        } else {
            definitions = DictionaryAPI.parseResponse(response);
        }
        raceWords.add(new CleanWord(word, definitions));
    }

    // ---- Command Overrides ----
    @Override
    public boolean addRacer(Member member) {
        if (players.get(member.getId()) == null) {
            players.put(member.getId(), new Racer(member));
            return true; // signal success
        }
        return false;
    }

    @Override
    public boolean removeRacer(String racerId) {
        if (players.get(racerId) != null) {
            players.remove(racerId);
            return true; // signal success
        }
        return false;
    }

    @Override
    public void incrementWords(String racerId) {
        Racer racer = players.get(racerId);
        if (racer != null) {
            racer.incrementWords();
        }
    }

    @Override
    public void setState(RaceState state) {
        this.state = state;
    }

    @Override
    public void setEndFuture(ScheduledFuture<Void> future) {
        raceEndFuture = future;
    }

    @Override
    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void cancelFuture() {
        if (raceEndFuture != null) {
            raceEndFuture.cancel(false);
        }
        cancelled = true;
    }

    @Override
    public void setTime() {
        this.time.setTemporals();
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

    @Nonnull
    @Override
    public RaceTime getTime() {
        return time;
    }

    @NotNull
    @Override
    public Message getMessage() {
        return message;
    }

    @NotNull
    @Override
    public String getLeaderboard() {
        StringBuilder leaderboard = new StringBuilder();
        // Calculate the winner
        List<Racer> sortedRacers = getPlayers()
                .stream()
                .sorted(Comparator.comparing(Racer::getWordsTyped).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < sortedRacers.size(); ++i) {
            leaderboard.append("**#").append(i + 1).append("** <@")
                    .append(sortedRacers.get(i).member.getId())
                    .append("> → Words Typed: ")
                    .append(sortedRacers.get(i).getWordsTyped()).append("\n");
        }

        return leaderboard.toString();
    }

    @NotNull
    @Override
    public String getGuildId() {
        return guildId;
    }

    @NotNull
    @Override
    public String getChannelId() {
        return channelId;
    }

    @NotNull
    @Override
    public String getEmojID() {
        return emojID;
    }
}
