package dracer.events;

import dracer.Dracer;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * All event initiations are managed by this class.
 */
public class EventDelegate extends ListenerAdapter {
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        Dracer.dracer.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("turbotastic!"));
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {

    }
}
