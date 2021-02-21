package dracer;

import dracer.util.Initialization;

public class DracerExec {
    public static void main(String[] args) {
        try {
            Dracer.initialize();
        } catch (Exception | Error e) {
            Initialization.lgr.error("Unable to start the bot. Details:", e);
            Dracer.immediateShutdown();
        }
    }
}
