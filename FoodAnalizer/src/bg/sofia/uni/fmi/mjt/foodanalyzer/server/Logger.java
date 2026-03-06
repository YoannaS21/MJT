package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class Logger {
    private static final String LOG_FILE = "server_errors.log";

    public static synchronized void logError(String message, Throwable t) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println("=== " + LocalDateTime.now() + " ===");
            out.println("Message: " + message);
            if (t != null) {
                t.printStackTrace(out);
            }
            out.println("--------------------------------");
        } catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }
}