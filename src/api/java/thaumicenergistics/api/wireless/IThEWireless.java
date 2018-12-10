package thaumicenergistics.api.wireless;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Contains wireless utilities
 *
 * @author BrockWS
 */
public interface IThEWireless {

    /**
     * Register a wireless gui handler
     *
     * @param clazz   Class of the Wireless GUI Handler
     * @param handler Wireless GUI Handler
     */
    <C extends IThEWirelessHandler> void registerWirelessHandler(Class<C> clazz, C handler);

    /**
     * Gets the registered wireless handler implementation for this handler class
     *
     * @param handler Handler class
     * @return Registered handler implementation
     */
    IThEWirelessHandler getWirelessHandler(Class<? extends IThEWirelessHandler> handler);

    /**
     * Gets the registered wireless handler implementation for an object
     *
     * @param obj    Object to check for
     * @param player Player that wants to open gui
     * @return Registered handler implementation
     */
    IThEWirelessHandler getWirelessHandler(Object obj, EntityPlayer player);

    /**
     * Checks if the Object has an associated handler
     *
     * @param obj    Object to check
     * @param player Player that wants to open gui
     * @return true if there is at least one handler that will handle this object
     */
    boolean isHandled(Object obj, EntityPlayer player);

    /**
     * Open a wireless gui for this object
     *
     * @param obj    Object that wants to open gui
     * @param player Player opening gui
     */
    void openGUI(Object obj, EntityPlayer player);
}
