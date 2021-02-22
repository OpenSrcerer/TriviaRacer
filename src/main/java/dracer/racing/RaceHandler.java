package dracer.racing;

import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;

public final class RaceHandler {
    // ChannelID, Race
    public static final Map<String, DictionaryRace> activeRaces = new HashMap<>();

    /**
     * @return True if race could be added, false if not.
     */
    public static boolean addRace(String channelId, Member startingMember) {
        /*if (!activeRaces.containsKey(channelId)) {
            activeRaces.put(channelId, DictionaryRace.getRace(startingMember));
            return true;
        }*/
        activeRaces.put(channelId, DictionaryRace.getRace(startingMember));
        return true;
    }

    public static void removeRace(String channelId) {
        activeRaces.remove(channelId);
    }
}
