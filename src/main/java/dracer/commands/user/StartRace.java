package dracer.commands.user;

import dracer.commands.Command;
import net.dv8tion.jda.api.events.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartRace implements Command {
    public static final Logger lgr = LoggerFactory.getLogger(StartRace.class);

    @Override
    public void run() {

    }

    @Override
    public Logger getLogger() {
        return lgr;
    }

    @Override
    public GenericEvent getEvent() {
        return null;
    }
}
