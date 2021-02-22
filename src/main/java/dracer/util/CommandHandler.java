package dracer.util;

import dracer.Dracer;
import dracer.commands.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.LinkedBlockingQueue;

public final class CommandHandler {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * Queue to hold command objects waiting to be taken by a worker thread.
     */
    private static final LinkedBlockingQueue<Command> commands = new LinkedBlockingQueue<>(100);

    static {
        Runnable drainCommands = () -> {
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }

                Command commandToProcess = null;
                try {
                    commandToProcess = commands.take(); // Take a command from the request queue
                    commandToProcess.run(); // Run the command
                } catch (RuntimeException ex) {
                    if (commandToProcess != null) {
                        // Usually permission errors
                        commandToProcess.getEvent().getChannel().sendMessage("Unable to run command: " + ex.getMessage()).queue();
                    }
                    lgr.error(Thread.currentThread().getName() + " encountered a runtime exception:", ex);
                } catch (Exception ex) {
                    // Other exceptions
                    lgr.error(Thread.currentThread().getName() + " encountered an exception:", ex);
                } catch (Error err) {
                    // Fatal Error, terminate program
                    lgr.error("A fatal error was thrown. Shutting down. Details:", err);
                    Dracer.immediateShutdown();
                }
            }
        };

        for (int thread = 1; thread <= Dracer.AVAILABLE_CORES; ++thread) {
            Dracer.COMMAND_EXECUTOR.submit(drainCommands);
        }
    }

    /**
     * Adds the command to the array of active commands.
     * @param command Command to be added.
     */
    public static void queueCommand(@Nonnull Command command) {
        try {
            commands.put(command);
        } catch (InterruptedException ex) {
            lgr.error("Request queue was interrupted!");
        }
    }
}
