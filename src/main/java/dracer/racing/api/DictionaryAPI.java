package dracer.racing.api;

import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.*;
import okhttp3.internal.annotations.EverythingIsNonNull;

import java.io.IOException;

public abstract class DictionaryAPI {
    private static final OkHttpClient client = new OkHttpClient();

    private static String DICTIONARY_KEY;
    private static String THESAURUS_KEY;
    private static final String API_URL = "https://www.dictionaryapi.com";

    public static void setKeys(String dKey, String tKey) {
        DICTIONARY_KEY = dKey;
        THESAURUS_KEY = tKey;

        client.a
    }

    public static void makeRequest() {
        Request request = new Request.Builder()
                .header("key", DICTIONARY_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException ex) {
                ex.printStackTrace();
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Something went wrong, code " + response);
                } else {

                }
            }
        });
    }

    private static HttpUrl buildUrl(String word) {
        final HttpUrl url = HttpUrl.parse(API_URL);

        if (url == null) {
            throw new IllegalArgumentException();
        }

        return url.newBuilder()
                .addQueryParameter("api", "api")
                .addQueryParameter("version", "v3")
                .addQueryParameter("ref", "references")
                .addQueryParameter("dictionary", "collegiate")
                .addQueryParameter("formatting", "json")
                .addQueryParameter("word", word)
                .build();
    }

    private static void parseResponse(Response response) throws IOException {
        try (ResponseBody body = response.body()) {
            if (body == null) {
                throw new IOException("Response body was null.");
            }

            DataObject json = DataObject.fromJson(body.string());

            return json.getArray("def")
        }
    }
}
