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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.*;

public abstract class TRacer {
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
    public static final int READING_TIME = 15;
    public static final int ANSWER_TIME = 5;
    public static final int ANSWER_VIEWING_TIME = 7;
    public static final int TRIVIA_QUESTION_TOTAL = READING_TIME + ANSWER_TIME + ANSWER_VIEWING_TIME;
    public static final int RACE_LENGTH = TASK_COUNT * TRIVIA_QUESTION_TOTAL;
    public static final int TOTAL_LENGTH = GRACE_PERIOD + RACE_LENGTH;

    // --- Files & Lists ---
    public static DataArray emojis;
    private static final List<String> cars = Arrays.asList(
            "<:wackymobile:815945750823174185>",
            "<:streetfuturistic:815945757588586517>",
            "<:stationwagon:815945754153320478>",
            "<:sedan:815945756581560320>",
            "<:cooper:815945753977159691>",
            "<:compact2:815945757689118830>",
            "<:compact:815945659114586112>",
            "<:beetle:815945751883808780>"
    );
    // ***************************************************************

    /**
     * Thread Pools for the bot.
     */
    public static final ExecutorService COMMAND_EXECUTOR;
    public static final ScheduledExecutorService RACE_EXECUTOR;

    /**
     * The JDA instance of the bot.
     */
    public static JDA tRacerInst;

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
                return new Thread(r, "RaceScheduler-" + counter++);
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

        Initialization.initEmojiFile("emojis-oliveratgithub.json");

        tRacerInst = JDABuilder
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

        Initialization.setConstants(config[0], tRacerInst.getSelfUser().getId(), tRacerInst.getSelfUser().getAvatarUrl());
        tRacerInst.getPresence().setPresence(OnlineStatus.DO_NOT_DISTURB, Activity.competing("loading..."));
    }

    public static String getRandomEmojis() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; ++i) {
            builder.append(emojis.getObject(ThreadLocalRandom.current().nextInt(4032)).getString("emoji"));
        }
        return builder.toString();
    }

    public static String getRandomCar() {
        return cars.get(ThreadLocalRandom.current().nextInt(cars.size()));
    }

    /**
     * Shuts down the bot in case of an Error/Exception thrown
     * or failure to initialize necessary configuration files.
     */
    public static void immediateShutdown() {
        COMMAND_EXECUTOR.shutdown();

        if (tRacerInst != null) {
            tRacerInst.shutdownNow();
        }

        System.exit(-1);
    }
}


