package com.thapar.ims.util;

import java.nio.file.Path;

/** Provides writable locations for both the source project and packaged desktop app. */
public final class AppPaths {
    private static final String APP_NAME = "Inventory Management System";

    private AppPaths() { }

    private static Path rootDirectory() {
        if (isPackaged()) {
            return Path.of(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        }
        return Path.of(System.getProperty("user.dir"));
    }

    private static boolean isPackaged() {
        return Boolean.getBoolean("ims.packaged")
                || System.getProperty("jpackage.app-path") != null;
    }

    public static Path dataDirectory() {
        return rootDirectory().resolve("data");
    }

    public static Path logsDirectory() {
        return rootDirectory().resolve("logs");
    }

    public static Path reportsDirectory() {
        return rootDirectory().resolve("reports");
    }
}
