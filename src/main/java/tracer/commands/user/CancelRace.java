package tracer.commands.user;

import tracer.commands.Command;
import tracer.racing.RaceHandler;
import tracer.racing.TriviaRace;
import tracer.util.CommandHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelRace implements Command {
    public static final Logger lgr = LoggerFactory.getLogger(CancelRace.class);
    private final GuildMessageReceivedEvent event;

    public CancelRace(GuildMessageReceivedEvent event) {
        this.event = event;
        CommandHandler.queueCommand(this);
    }

    @Override
    public void run() {
        if (RaceHandler.findRace(event.getChannel().getId())) {
            TriviaRace race = RaceHandler.getRace(event.getChannel().getId());

            //noinspection ConstantConditions
            if (race.getOwnerId().equals(event.getMember().getId()) && race.getState().equals(TriviaRace.RaceState.STARTING)) {
                race.cancel();
                RaceHandler.removeRace(event.getChannel().getId());
            } else {
                event.getChannel().sendMessage("<@" + event.getMember().getId() + "> You may not cancel a race that is already running, or if you did not start it.").queue();
            }
        } else {
            // noinspection ConstantConditions
            event.getChannel().sendMessage("<@" + event.getMember().getId() + "> A race isn't running in this channel.").queue();
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
