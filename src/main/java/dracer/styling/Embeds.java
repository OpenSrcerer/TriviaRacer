package dracer.styling;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Instant;

public class Embed extends EmbedBuilder {
    /**
     * Default color for all Embeds.
     */
    private static final int embedColor = 0x2f3136;

    public enum EmbedType {
        RACEEND
    }

    public static MessageEmbed getEmbed(EmbedType type) {
        return new Embed(type).build();
    }

    private Embed(EmbedType type) {
        super();
        setColor(embedColor);
        setTimestamp(Instant.now());

        switch (type) {
            case RACEEND -> raceEmbed();
        }
    }

    private void raceEmbed() {

    }
}
