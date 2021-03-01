package tracer.commands.user;

import tracer.commands.Command;
import tracer.racing.TriviaRace;
import tracer.racing.RaceHandler;
import tracer.util.CommandHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinRace implements Command {
    public static final Logger lgr = LoggerFactory.getLogger(StartRace.class);
    private final GuildMessageReceivedEvent event;

    public JoinRace(GuildMessageReceivedEvent event) {
        this.event = event;
        CommandHandler.queueCommand(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        if (RaceHandler.findRace(event.getChannel().getId())) {
            TriviaRace race = RaceHandler.getRace(event.getChannel().getId());
            if (race == null) {
                event.getChannel().sendMessage("<@" + event.getMember().getId() + "> you are already part of this race!").queue();
            } else {
                if (race.getState().equals(TriviaRace.RaceState.STARTING)) {
                    RaceHandler.addRacer(event.getChannel().getId(), event.getMember());
                    RaceHandler.refreshStartingMessage(race);
                } else {
                    event.getChannel().sendMessage("<@" + event.getMember().getId() + "> You cannot join a race that is in progress!").queue();
                }
            }
        } else {
            event.getChannel().sendMessage("<@" + event.getMember().getId() + "> A race has not been started yet!").queue();
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
