package tracer.commands.user;

import tracer.commands.Command;
import tracer.racing.TriviaRace;
import tracer.racing.RaceHandler;
import tracer.util.CommandHandler;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveRace implements Command {
    public static final Logger lgr = LoggerFactory.getLogger(LeaveRace.class);
    private final GuildMessageReceivedEvent event;

    public LeaveRace(GuildMessageReceivedEvent event) {
        this.event = event;
        CommandHandler.queueCommand(this);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void run() {
        if (RaceHandler.findRace(event.getChannel().getId())) {
            TriviaRace race = RaceHandler.getRace(event.getChannel().getId());
            if (race == null) {
                event.getChannel().sendMessage("<@" + event.getMember().getId() + "> You are not part of this race!").queue();
            } else {
                if (race.getState().equals(TriviaRace.RaceState.STARTING)) {
                    if (race.getOwnerId().equals(event.getMember().getId())) {
                        event.getChannel().sendMessage("<@" + event.getMember().getId() + "> You're the owner dumdum, " +
                                "why do you wanna leave?! Cancel the race instead with `tcr.cancel`.").queue();
                    } else {
                        RaceHandler.removeRacer(event.getChannel().getId(), event.getMember().getId());
                        RaceHandler.refreshStartingMessage(race);
                    }
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

