package dracer.racing.api;

import dracer.Dracer;
import dracer.racing.DictionaryRace;
import net.dv8tion.jda.api.utils.data.DataArray;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DictionaryAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en_US/";

    public static void getWords(DictionaryRace race, int wordsToGet) {
        CompletableFuture.supplyAsync(() -> {
            for (String word : getRandomWords(wordsToGet)) {
                Request request = new Request.Builder()
                        .url(buildUrl(word))
                        .header("word", word)
                        .build();

                client.newCall(request).enqueue(race);
            }
            return null;
        }, Dracer.RACE_EXECUTOR
        );
    }

    private static HttpUrl buildUrl(String word) {
        final HttpUrl url = HttpUrl.parse(API_URL.concat(word));

        if (url == null) {
            throw new IllegalArgumentException();
        }

        return url.newBuilder().build();
    }

    public static List<String> parseResponse(Response response) throws IOException {
        List<String> definitions = new ArrayList<>();
        try (ResponseBody body = response.body()) {
            if (body == null) {
                throw new IOException("Response body was null.");
            }
            try {
                DataArray json = DataArray.fromJson(body.string()); // Consume the body once
                DataArray meanings = json.getObject(0).getArray("meanings");

                if (meanings.isEmpty()) {
                    definitions.add("No meanings found for this one :(");
                } else {
                    DataArray defs = meanings.getObject(0).getArray("definitions");
                    for (Object def : defs) {
                        HashMap<String, String> definition = (HashMap<String, String>) def;
                        definitions.add(definition.get("definition"));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return definitions;
    }

    private static List<String> getRandomWords(int wordsToGet) {
        List<String> wordList = new ArrayList<>();

        for (int i = 0; i < wordsToGet; ++i) {
            wordList.add(Dracer.cleanWords.get(ThreadLocalRandom.current().nextInt(Dracer.cleanWords.size())));
        }

        return wordList;
    }
}
