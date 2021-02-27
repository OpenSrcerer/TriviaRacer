package dracer.racing.api;

import dracer.Dracer;
import dracer.racing.entities.WordDefPair;
import net.dv8tion.jda.api.utils.data.DataArray;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public abstract class DictionaryAPI {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en_US/";

    private static HttpUrl buildUrl(String word) {
        final HttpUrl url = HttpUrl.parse(API_URL.concat(word));

        if (url == null) {
            throw new IllegalArgumentException();
        }

        return url.newBuilder().build();
    }

    public static WordDefPair getWordWithDef() {
        String definition = null;
        String word = null;

        try {
            while (definition == null) {
                word = Dracer.cleanWords.get(ThreadLocalRandom.current().nextInt(Dracer.cleanWords.size()));

                Request request = new Request.Builder()
                    .url(buildUrl(word))
                    .build();
                ResponseBody body = client.newCall(request).execute().body();
                if (body == null) {
                    throw new IOException("Response body was null.");
                }
                DataArray json = DataArray.fromJson(body.string());
                DataArray meanings = json.getObject(0).getArray("meanings");

                if (!meanings.isEmpty()) {
                    DataArray defs = meanings.getObject(0).getArray("definitions");
                    definition = defs.getObject(0).getString("definition");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new WordDefPair(word, definition);
    }
}
