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

    public static void info(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().info(message, args);
    }

    public static void error(String message, Object... args) {
        if (ThELog.getLogger() != null)
            ThELog.getLogger().error(message, args);
    }
}
