package dracer;

import dracer.events.EventDelegate;
import dracer.util.Initialization;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.data.DataArray;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.concurrent.*;

public abstract class Dracer {
    // ***************************************************************
    // **                        CONSTANTS                          **
    // ***************************************************************
    public static String DEFAULT_PREFIX;
    public static String USER_ID;
    public static String AVATAR_URL;
    public static final int AVAILABLE_CORES = Math.max(Runtime.getRuntime().availableProcessors(), 2);

    // --- Racing Constants ---
    public static final int TASK_COUNT = 10;
    public static final int GRACE_PERIOD = 20;
    public static final int RACE_LENGTH = 60;
    public static final int TOTAL_LENGTH = GRACE_PERIOD + RACE_LENGTH;

    // --- Word Files ---
    public static ArrayList<String> cleanWords;
    public static ArrayList<String> offensiveWords;
    public static DataArray emojis;
    // ***************************************************************

    /**
     * Thread Pools for the bot.
     */
    public static final ExecutorService COMMAND_EXECUTOR;
    public static final ScheduledExecutorService RACE_EXECUTOR;

    /**
     * The JDA instance of the bot.
     */
    public static JDA dracerInst;

    // Initializing for the Thread Factory & Pool
    static {
        ThreadFactory nonScheduledFactory = new ThreadFactory() {
            private int counter = 1;

            @Override
            public Thread newThread(@Nonnull final Runnable r) {
                return new Thread(r, "Commander-" + counter++);
            }
        };

        ThreadFactory scheduledFactory = new ThreadFactory() {
            private int counter = 1;

            @Override
            public Thread newThread(@Nonnull final Runnable r) {
                return new Thread(r, "Handler-" + counter++);
            }
        };

        COMMAND_EXECUTOR = Executors.newFixedThreadPool(AVAILABLE_CORES/2, nonScheduledFactory);
        RACE_EXECUTOR = Executors.newScheduledThreadPool(AVAILABLE_CORES/2, scheduledFactory);
    }

    /**
     * Start Thermostat and initialize all needed variables.
     * @throws Exception Any Exception that may occur.
     * @throws Error Any Error that may occur while loading.
     */
    protected static void initialize() throws Exception, Error {
        // Get the configuration values
        final String[] config = Initialization.initializeTokens();

        Initialization.initFiles("clean.txt", "offensive.txt", "emojis-oliveratgithub.json");

        dracerInst = JDABuilder
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

        Initialization.setConstants(config[0], dracerInst.getSelfUser().getId(), dracerInst.getSelfUser().getAvatarUrl());
        dracerInst.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("loading..."));
    }

    public static String getRandomEmojis() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            builder.append(emojis.getObject(ThreadLocalRandom.current().nextInt(4032)).getString("emoji"));
        }
        return builder.toString();
    }

    /**
     * Shuts down the bot in case of an Error/Exception thrown
     * or failure to initialize necessary configuration files.
     */
    public static void immediateShutdown() {
        COMMAND_EXECUTOR.shutdown();

        if (dracerInst != null) {
            dracerInst.shutdownNow();
        }

        System.exit(-1);
    }
}


