package dracer.commands;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;

public interface Command extends Runnable {
    /**
     * Execute the Command's Discord-side code.
     */
    void run();

    /**
     * @return The Logger for the specific Command.
     */
    Logger getLogger();

    /**
     * Get the Command's data package.
     * @return The Command's information. (Event, Prefix, etc.)
     */
    GuildMessageReceivedEvent getEvent();
}
