package dracer.events;

import dracer.TRacer;
import dracer.commands.user.CancelRace;
import dracer.commands.user.JoinRace;
import dracer.commands.user.LeaveRace;
import dracer.commands.user.StartRace;
import dracer.racing.RaceHandler;
import dracer.racing.TriviaRace;
import dracer.styling.Embed;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * All event initiations are managed by this class.
 */
@SuppressWarnings("ConstantConditions")
public class EventDelegate extends ListenerAdapter {
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        TRacer.tRacerInst.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("tcr.help -> turbotastic!"));
    }

    @Override
    public void onGuildMessageReceived(@Nonnull final GuildMessageReceivedEvent event) {
        if (!validateEvent(event)) { // If the event is invalid or the member is a bot, don't reply
            return;
        }

        // Get the arguments from the event and split them appropriately.
        ArrayList<String> arguments = new ArrayList<>(Arrays.asList(event.getMessage().getContentRaw().split("\\s+")));

        switch (arguments.get(0)) {
            case "tcr.start" -> new StartRace(event, arguments);
            case "tcr.cancel" -> new CancelRace(event);
            case "tcr.join" -> new JoinRace(event);
            case "tcr.leave" -> new LeaveRace(event);
            case "tcr.help" -> event.getChannel().sendMessage(Embed.EmbedFactory(null, Embed.EmbedType.HELP)).queue();
            // case "tcr.vote" -> event.getChannel().sendMessage(Embed.EmbedFactory(null, Embed.EmbedType.VOTE)).queue(); Disabled Temporarily
            case "tcr.other" -> event.getChannel().sendMessage(Embed.EmbedFactory(null, Embed.EmbedType.OTHER)).queue();
            case "tcr.science" -> event.getChannel().sendMessage(Embed.EmbedFactory(null, Embed.EmbedType.SCIENCE)).queue();
            case "tcr.entertainment" -> event.getChannel().sendMessage(Embed.EmbedFactory(null, Embed.EmbedType.ENTERTAINMENT)).queue();
        }
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!validateEvent(event)) { // If the event is invalid or the member is a bot, don't reply
            return;
        }

        if (RaceHandler.isChannelRaceMode(event.getChannel().getId())) {
            TriviaRace race = RaceHandler.getRace(event.getChannel().getId());
            String emoji = event.getReactionEmote().getAsReactionCode();

            if (event.getMessageId().equals(race.getMessage().getId())) {
                if (!RaceHandler.evaluateAnswer(event.getChannel().getId(), event.getMember().getId(), emoji)) {
                    race.getMessage().removeReaction(emoji, event.getUser()).queue();
                }
            }
        }
    }

    public static boolean validateEvent(final GuildMessageReceivedEvent event) {
        if (event.getMember() == null)
            return false;

        return !event.getMember().getUser().isBot();
    }

    public static boolean validateEvent(final GuildMessageReactionAddEvent event) {
        if (event.getMember() == null)
            return false;

        return !event.getMember().getUser().isBot();
    }
}
