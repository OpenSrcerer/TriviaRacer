package dracer;

import dracer.events.EventDelegate;
import dracer.racing.api.DictionaryAPI;
import dracer.util.Initialization;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public abstract class Dracer {
    // ***************************************************************
    // **                        CONSTANTS                          **
    // ***************************************************************
    public static String DEFAULT_PREFIX;
    public static String USER_ID;
    public static String AVATAR_URL;
    public static final int AVAILABLE_CORES = Math.max(Runtime.getRuntime().availableProcessors(), 2);

    // --- Word Files ---
    public static ArrayList<String> cleanWords;
    public static ArrayList<String> offensiveWords;
    // ***************************************************************

    /**
     * Thread Pool for the bot.
     */
    public static final ExecutorService NON_SCHEDULED_EXECUTOR;

    /**
     * The JDA instance of the bot.
     */
    public static JDA dracer;

    // Initializing for the Thread Factory & Pool
    static {
        ThreadFactory nonScheduledFactory = new ThreadFactory() {
            private int counter = 1;

            @Override
            public Thread newThread(@Nonnull final Runnable r) {
                return new Thread(r, "Commander-" + counter++);
            }
        };

        NON_SCHEDULED_EXECUTOR = Executors.newFixedThreadPool(AVAILABLE_CORES/2, nonScheduledFactory);
    }

    /**
     * Start Thermostat and initialize all needed variables.
     * @throws Exception Any Exception that may occur.
     * @throws Error Any Error that may occur while loading.
     */
    protected static void initialize() throws Exception, Error {
        // Get the configuration values
        final String[] config = Initialization.initializeTokens();

        Initialization.setConstants(config[0], dracer.getSelfUser().getId(), dracer.getSelfUser().getAvatarUrl());
        Initialization.initializeDefaultFiles("clean.txt", "offensive.txt");
        DictionaryAPI.setKeys(config[2], config[3]);

        dracer = JDABuilder
                .create(
                        config[1],
                        EnumSet.of(
                                GatewayIntent.GUILD_MESSAGES,
                                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                                GatewayIntent.GUILD_EMOJIS
                        )
                )
                .enableCache(CacheFlag.EMOTE)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.MEMBER_OVERRIDES,
                        CacheFlag.VOICE_STATE
                )
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setEnableShutdownHook(true)
                .addEventListeners(new EventDelegate())
                .build();

        dracer.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("loading..."));
    }

    /**
     * Shuts down the bot in case of an Error/Exception thrown
     * or failure to initialize necessary configuration files.
     */
    public static void immediateShutdown() {
        NON_SCHEDULED_EXECUTOR.shutdown();

        if (dracer != null) {
            dracer.shutdownNow();
        }

        System.exit(-1);
    }
}


