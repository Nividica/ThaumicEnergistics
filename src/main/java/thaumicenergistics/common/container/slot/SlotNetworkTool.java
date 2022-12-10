package thaumicenergistics.common.container.slot;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Represents a slot in the network tool
 *
 * @author Nividica
 *
 */
public class SlotNetworkTool extends Slot {
    /**
     * Creates the slot.
     *
     * @param inventory
     * The inventory of the network tool.
     * @param slotIndex
     * Index of the slot
     * @param xDisplayPosition
     * X position of the slot
     * @param yDisplayPosition
     * Y position of the slot
     */
    public SlotNetworkTool(
            final INetworkTool inventory, final int slotIndex, final int xDisplayPosition, final int yDisplayPosition) {
        // Pass to super
        super(inventory, slotIndex, xDisplayPosition, yDisplayPosition);
    }

    /**
     * Called to see if an item can go in this inventory
     */
    @Override
    public boolean isItemValid(final ItemStack itemStack) {
        // Is the itemstack valid?
        if (itemStack != null) {
            // Get the item
            Item item = itemStack.getItem();

            // Is the item an upgrade card?
            if (item instanceof IUpgradeModule) {
                // True if a valid upgrade
                return ((IUpgradeModule) item).getType(itemStack) != null;
            }
        }

        // Not an upgrade or invalid itemstack
        return false;
    }
}
