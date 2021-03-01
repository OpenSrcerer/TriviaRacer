package tracer;

import tracer.util.Initialization;

public class TRacerExec {
    public static void main(String[] args) throws InterruptedException {
        try {
            TRacer.initialize();
        } catch (Exception | Error e) {
            Initialization.lgr.error("Unable to start the bot. Details:", e);
            TRacer.immediateShutdown();
        }
    }
}
