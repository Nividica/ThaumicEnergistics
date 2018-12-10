package thaumicenergistics.api.wireless;

import com.google.common.base.Preconditions;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * @author BrockWS
 */
public interface IThEWirelessHandler {

    default void registerObject(IThEWirelessObject obj) {
        Preconditions.checkNotNull(obj);
        Preconditions.checkArgument(!this.getRegisteredObjects().contains(obj));

        this.getRegisteredObjects().add(obj);
    }

    default boolean canHandle(Object obj, EntityPlayer player) {
        if (obj instanceof IThEWirelessObject)
            return this.getRegisteredObjects().contains(obj) && ((IThEWirelessObject) obj).isHandledBy(this, obj, player);

        if (obj instanceof ItemStack && !((ItemStack) obj).isEmpty() && ((ItemStack) obj).getItem() instanceof IThEWirelessObject) {
            IThEWirelessObject wirelessObject = (IThEWirelessObject) ((ItemStack) obj).getItem();
            return this.getRegisteredObjects().contains(wirelessObject) && wirelessObject.isHandledBy(this, obj, player);
        }
        return false;
    }

    void openGUI(Object obj, EntityPlayer player);

    List<IThEWirelessObject> getRegisteredObjects();
}
