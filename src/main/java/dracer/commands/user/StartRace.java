package dracer.commands.user;

import dracer.commands.Command;
import dracer.racing.RaceHandler;
import dracer.util.CommandHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartRace implements Command {
    public static final Logger lgr = LoggerFactory.getLogger(StartRace.class);
    private final GuildMessageReceivedEvent event;

    public StartRace(GuildMessageReceivedEvent event) {
        this.event = event;
        CommandHandler.queueCommand(this);
    }

    @Override
    public void run() {
        if (!RaceHandler.isRaceActive(event.getChannel().getId())) {
            RaceHandler.addRace(event.getChannel(), event.getMember());
        } else {
            // noinspection ConstantConditions
            event.getChannel().sendMessage("<@" + event.getMember().getId() + "> A race is already running in this channel!").queue();
        }
    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return event;
    }
}
