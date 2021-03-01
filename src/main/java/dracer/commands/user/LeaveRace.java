package dracer.commands.user;

import dracer.commands.Command;
import dracer.racing.TriviaRace;
import dracer.racing.RaceHandler;
import dracer.util.CommandHandler;
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
        if (RaceHandler.isRaceActive(event.getChannel().getId())) {
            TriviaRace race = RaceHandler.removeRacer(event.getChannel().getId(), event.getMember().getId());
            if (race == null) {
                event.getChannel().sendMessage("<@" + event.getMember().getId() + "> You are not part of this race!").queue();
            } else {
                RaceHandler.refreshRaceMessage(race);
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

