package thaumicenergistics.common.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Wrapper class to pass validation to inventory
 *
 * @author Nividica
 *
 */
public class SlotRestrictive extends Slot {
    private int myIndex;

    /**
     * Create the slot.
     *
     * @param inventory
     * Inventory it belongs to.
     * @param index
     * Index of the slot
     * @param x
     * X position of the slot
     * @param y
     * Y position of the slot
     */
    public SlotRestrictive(final IInventory inventory, final int index, final int x, final int y) {
        // Pass to super
        super(inventory, index, x, y);

        // Set this index
        this.myIndex = index;
    }

    /**
     * Pass the is valid call to the inventory.
     */
    @Override
    public boolean isItemValid(final ItemStack itemstack) {
        return this.inventory.isItemValidForSlot(this.myIndex, itemstack);
    }
}
