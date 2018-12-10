package thaumicenergistics.api.wireless;

import net.minecraft.entity.player.EntityPlayer;

/**
 * @author BrockWS
 */
public interface IThEWirelessObject {

    default boolean isHandledBy(IThEWirelessHandler handler, Object obj, EntityPlayer player) {
        return true;
    }

    boolean hasPower(double amount);

    boolean usePower(double amount);
}
