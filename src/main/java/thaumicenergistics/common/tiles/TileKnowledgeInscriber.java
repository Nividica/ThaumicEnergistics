package thaumicenergistics.common.tiles;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumicenergistics.common.items.ItemKnowledgeCore;
import thaumicenergistics.common.tiles.abstraction.ThETileInventory;

/**
 * Encodes arcane recipes.
 *
 * @author Nividica
 *
 */
public class TileKnowledgeInscriber extends ThETileInventory {
    public static final int KCORE_SLOT = 0;

    private static final String NBTKEY_KCORE = "kcore";

    public TileKnowledgeInscriber() {
        super("knowledge.inscriber", 1, 64);
    }

    /**
     * Returns true if there is a stored KCore.
     *
     * @return
     */
    public boolean hasKCore() {
        return this.internalInventory.getHasStack(KCORE_SLOT);
    }

    @Override
    public boolean isItemValidForSlot(final int slotIndex, final ItemStack itemStack) {
        // Null is always allowed
        if (itemStack == null) {
            return true;
        }

        // KCORE slot?
        if (slotIndex == KCORE_SLOT) {
            // Ensure the item is a KCORE
            return (itemStack.getItem() instanceof ItemKnowledgeCore);
        }

        return true;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        // Call super
        super.readFromNBT(data);

        // Is there a saved core?
        if (data.hasKey(NBTKEY_KCORE)) {
            // Load the saved core
            this.internalInventory.setInventorySlotContents(
                    KCORE_SLOT, ItemStack.loadItemStackFromNBT(data.getCompoundTag(NBTKEY_KCORE)));
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        // Call super
        super.writeToNBT(data);

        // Get the kcore
        ItemStack kCore = this.internalInventory.getStackInSlot(KCORE_SLOT);
        if (kCore != null) {
            // Write the kcore
            data.setTag(NBTKEY_KCORE, kCore.writeToNBT(new NBTTagCompound()));
        }
    }
}
