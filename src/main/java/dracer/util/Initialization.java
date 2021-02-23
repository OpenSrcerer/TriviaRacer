package dracer.util;

import dracer.Dracer;
import dracer.DracerExec;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public final class Initialization {
    public static final Logger lgr = LoggerFactory.getLogger(Initialization.class);

    /**
     * Initialize Dracer's constants.
     */
    public static void setConstants(String pref, String id, String url) {
        Dracer.DEFAULT_PREFIX = pref;
        Dracer.USER_ID = id;
        Dracer.AVATAR_URL = url;
    }

    /**
     * Reads the config.json file and parses the data into a usable
     * array of strings.
     * @return Array of configuration tokens.
     * @throws ParsingException If I/O operations had an issue.
     */
    public static String[] initializeTokens() throws ParsingException {
        String[] tokens = new String[2];

        InputStream configFile = DracerExec.class.getClassLoader().getResourceAsStream("config.json");

        if (configFile == null) {
            lgr.error("JSON config file not found or could not be accessed.");
            return tokens;
        }

        DataObject config = DataObject.fromJson(configFile);
        tokens[0] = config.getString("Prefix");
        tokens[1] = config.getString("JDAToken");

        return tokens;
    }

    public static void initFiles(String file1, String file2, String file3) throws Exception {
        Dracer.cleanWords = retrieveWordFile(file1);
        Dracer.offensiveWords = retrieveWordFile(file2);
        try (InputStream emojisFile = DracerExec.class.getClassLoader().getResourceAsStream(file3)) {
            if (emojisFile == null) {
                throw new NullPointerException("Emoji file not found");
            }
            Dracer.emojis = DataObject.fromJson(emojisFile).getArray("emojis");
        }

        lgr.info("Loaded files " + file1 + ", " + file2 + " and " + file3 + ".");
    }

    private static ArrayList<String> retrieveWordFile(String fileName) throws Exception {
        final ArrayList<String> wordFileArray = new ArrayList<>();

        try (InputStream fileStream = DracerExec.class.getClassLoader().getResourceAsStream(fileName)) {
            if (fileStream == null) {
                throw new FileNotFoundException("Could not find a file with given file name.");
            }

            try (Scanner scanner = new Scanner(fileStream)) {
                scanner.useDelimiter(",+");

                while (scanner.hasNext()) {
                    wordFileArray.add(scanner.next());
                }
            }
        }

        if (wordFileArray.isEmpty()) {
            throw new NullPointerException("Stream for file " + fileName + " provided no elements");
        }

        return wordFileArray;
    }
}
