package tracer.util;

import tracer.TRacer;
import tracer.TRacerExec;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

public final class Initialization {
    public static final Logger lgr = LoggerFactory.getLogger(Initialization.class);

    /**
     * Initialize Dracer's constants.
     */
    public static void setConstants(String pref, String id, String url) {
        TRacer.DEFAULT_PREFIX = pref;
        TRacer.USER_ID = id;
        TRacer.AVATAR_URL = url;
    }

    /**
     * Reads the config.json file and parses the data into a usable
     * array of strings.
     * @return Array of configuration tokens.
     * @throws ParsingException If I/O operations had an issue.
     */
    public static String[] initializeTokens() throws ParsingException {
        String[] tokens = new String[2];

        InputStream configFile = TRacerExec.class.getClassLoader().getResourceAsStream("config.json");

        if (configFile == null) {
            lgr.error("JSON config file not found or could not be accessed.");
            return tokens;
        }

        DataObject config = DataObject.fromJson(configFile);
        tokens[0] = config.getString("Prefix");
        tokens[1] = config.getString("JDAToken");

        return tokens;
    }

    public static void initEmojiFile(String file1) throws Exception {
        try (InputStream emojisFile = TRacerExec.class.getClassLoader().getResourceAsStream(file1)) {
            if (emojisFile == null) {
                throw new NullPointerException("Emoji file not found");
            }
            TRacer.emojis = DataObject.fromJson(emojisFile).getArray("emojis");
        }

        lgr.info("Loaded emoji file " + file1 + ".");
    }
}
