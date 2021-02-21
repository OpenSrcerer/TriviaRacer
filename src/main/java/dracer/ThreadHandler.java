package dracer;

import dracer.commands.Command;
import dracer.util.PermissionComputer;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.LinkedBlockingQueue;

public final class ThreadHandler {
    /**
     * Logger for this class.
     */
    private static final Logger lgr = LoggerFactory.getLogger(ThreadHandler.class);

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
                        // Usually permission errors.
                        ResponseDispatcher.commandFailed(commandToProcess,
                                Embeds.getEmbed(EmbedType.ERR, commandToProcess.getData(), ex.getMessage())
                        );
                    }
                    lgr.error(Thread.currentThread().getName() + " encountered a runtime exception:", ex);
                } catch (Exception ex) {
                    // Other exceptions
                    lgr.error(Thread.currentThread().getName() + " encountered an exception:", ex);
                } catch (Error err) {
                    // Fatal Error, terminate program
                    lgr.error("A fatal error was thrown. Shutting down Thermostat. Details:", err);
                    Dracer.immediateShutdown();
                }
            }
        };

        for (int thread = 1; thread <= Dracer.AVAILABLE_CORES; ++thread) {
            Dracer.NON_SCHEDULED_EXECUTOR.submit(drainCommands);
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
            ResponseDispatcher.commandFailed(
                    command,
                    Embeds.getEmbed(EmbedType.ERR_FIX, command.getData(),
                            Arrays.asList("Something went wrong on our end while handling your command.",
                                    "Please try again."
                            )
                    ), ex
            );
        }
    }

    /**
     * Adds a Command to the Command queue if Thermostat has the right permissions.
     * @param command Command to add to queue.
     */
    public static void checkThermoPermissionsAndQueue(@Nonnull final Command command) {
        final GenericEvent commandEvent = command.getEvent();

        commandEvent.getGuild().retrieveMember(Dracer.dracer.getSelfUser())
                .map(thermostat -> {
                    EnumSet<Permission> missingThermostatPerms = PermissionComputer.getMissingPermissions(thermostat,
                            commandEvent.getChannel(), command.getType().getThermoPerms());

                    if (missingThermostatPerms.isEmpty()) {
                        queueCommand(command);
                    } else {
                        command.getLogger().info("Missing permissions on (" + commandEvent.getGuild().getName() +
                                "/" + commandEvent.getGuild().getId() + "):" +
                                " " + missingThermostatPerms.toString() + "");

                    }
                    return thermostat;
                }).queue();
    }
}
