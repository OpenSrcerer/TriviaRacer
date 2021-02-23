package dracer.events;

import dracer.Dracer;
import dracer.commands.user.JoinRace;
import dracer.commands.user.StartRace;
import dracer.racing.RaceHandler;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * All event initiations are managed by this class.
 */
public class EventDelegate extends ListenerAdapter {
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        Dracer.dracerInst.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("turbotastic!"));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onGuildMessageReceived(@Nonnull final GuildMessageReceivedEvent event) {
        if (!validateEvent(event)) { // If the event is invalid or the member is a bot, don't reply
            return;
        }

        if (RaceHandler.isChannelRaceMode(event.getChannel().getId())) {
            RaceHandler.incrementWordsForRacer(event.getChannel().getId(), event.getMember().getId());
            event.getMessage().delete().queue(null, null);
            return;
        }

        // Get the arguments from the event and split them appropriately.
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split("\\s+")));

        switch (arguments.get(0)) {
            case "dcr.start" -> new StartRace(event);
            case "dcr.join" -> new JoinRace(event);
        }
    }

    public static boolean validateEvent(final GuildMessageReceivedEvent event) {
        if (event.getMember() == null)
            return false;

        return !event.getMember().getUser().isBot();
    }
}
