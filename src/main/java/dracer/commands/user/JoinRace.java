package dracer.commands.user;

import dracer.commands.Command;
import dracer.racing.DictionaryRace;
import dracer.racing.RaceHandler;
import dracer.styling.Embed;
import dracer.util.CommandHandler;
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
        if (RaceHandler.isRaceActive(event.getChannel().getId())) {
            DictionaryRace race = RaceHandler.addRacerToRace(event.getChannel().getId(), event.getMember());
            if (race == null) {
                event.getChannel().sendMessage("<@" + event.getMember().getId() + "> you are already part of this race!").queue();
            } else {
                race.getMessage().editMessage(Embed.EmbedFactory(race, Embed.EmbedType.STARTING)).queue();
            }
        } else {
            event.getChannel().sendMessage("<@" + event.getMember().getId() + "> A race has not been started yet!").queue();
        }
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public GuildMessageReceivedEvent getEvent() {
        return null;
    }
}
