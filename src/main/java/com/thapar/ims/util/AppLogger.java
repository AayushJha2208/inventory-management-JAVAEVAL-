package com.thapar.ims.util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Java Util Logging (built into the JDK).
 * One logger name for the whole project. Messages go to the console and to logs/app.log.
 */
public final class AppLogger {

    private static final Logger LOGGER = Logger.getLogger("com.thapar.ims");

    static {
        LOGGER.setLevel(Level.INFO);
        LOGGER.setUseParentHandlers(false);

        ConsoleHandler console = new ConsoleHandler();
        console.setLevel(Level.INFO);
        LOGGER.addHandler(console);

        try {
            java.nio.file.Path folder = AppPaths.logsDirectory();
            java.nio.file.Files.createDirectories(folder);
            FileHandler file = new FileHandler(folder.resolve("app.log").toString(), true);
            file.setFormatter(new SimpleFormatter());
            file.setLevel(Level.INFO);
            LOGGER.addHandler(file);
        } catch (IOException ex) {
            LOGGER.warning("Log file could not be created: " + ex.getMessage());
        }
    }

    private AppLogger() {
    }

    public static Logger get(Class<?> type) {
        return Logger.getLogger(type.getName());
    }
}
