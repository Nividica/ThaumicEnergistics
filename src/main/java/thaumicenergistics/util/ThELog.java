package thaumicenergistics.util;

import org.apache.logging.log4j.Logger;

import thaumicenergistics.ThaumicEnergistics;

/**
 * @author BrockWS
 */
public class ThELog {

    public static Logger getLogger() {
        return ThaumicEnergistics.LOGGER;
    }

    public static void error(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().error(message, args);
    }

    public static void error(String message, Throwable throwable) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().error(message, throwable);
    }

    public static void warn(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().warn(message, args);
    }

    public static void info(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().info(message, args);
    }

    public static void debug(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().debug(message, args);
    }

    public static void trace(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().trace(message, args);
    }
}
