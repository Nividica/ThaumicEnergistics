package thaumicenergistics.common.inventory;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import thaumicenergistics.common.utils.ThEUtils;

/**
 * Manages an internal inventory.
 *
 * @author Nividica
 *
 */
public class TheInternalInventory implements IInventory {

    /**
     * NBT keys
     */
    private static final String NBT_KEY_SLOT = "Slot";

    /**
     * Stack size limit.
     */
    private final int stackLimit;

    /**
     * The inventory slots.
     */
    private final ItemStack[] slots;

    /**
     * Name of the inventory.
     */
    private final String customName;

    /**
     * Creates the inventory.
     *
     * @param customName
     * @param size
     * @param stackLimit
     */
    public TheInternalInventory(final String customName, final int size, final int stackLimit) {
        this.slots = new ItemStack[size];
        this.customName = customName;
        this.stackLimit = stackLimit;
    }

    @Override
    public void closeInventory() {}

    @Override
    public ItemStack decrStackSize(final int slotIndex, final int amount) {
        // Get the stack
        ItemStack slotStack = this.slots[slotIndex];
        ItemStack resultStack = null;

        // Is the slot empty?
        if (slotStack == null) {
            // Slot is empty
            return null;
        }

        // Calculate the amount to get
        int decAmount = Math.min(amount, slotStack.stackSize);

        // Was any amount gotten?
        if (decAmount > 0) {
            resultStack = slotStack.copy();
            resultStack.stackSize = decAmount;
        }

        // Calculate the remaining amount
        slotStack.stackSize = slotStack.stackSize - decAmount;

        // Is there anything remaining in the slot?
        if (slotStack.stackSize > 0) {
            // Re-set
            this.setInventorySlotContents(slotIndex, slotStack);
        } else {
            // Clear
            this.setInventorySlotContents(slotIndex, null);
        }

        return resultStack;
    }

    /**
     * Returns true if there is a stack in the specified slot.
     *
     * @param slotIndex
     * @return
     */
    public boolean getHasStack(final int slotIndex) {
        return this.slots[slotIndex] != null;
    }

    @Override
    public String getInventoryName() {
        return this.customName;
    }

    @Override
    public int getInventoryStackLimit() {
        return this.stackLimit;
    }

    @Override
    public int getSizeInventory() {
        return this.slots.length;
    }

    @Override
    public ItemStack getStackInSlot(final int slotIndex) {
        return this.slots[slotIndex];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(final int slotIndex) {
        return this.slots[slotIndex];
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    /**
     * Increases the stack size of the specified slot, and marks the inventory as dirty.
     *
     * @param slotIndex
     * @param amount
     * @return The amount that was added.
     */
    public int incrStackSize(final int slotIndex, final int amount) {
        // Get the stack
        ItemStack slotStack = this.slots[slotIndex];

        // Assume none was added
        int added = 0;

        if (slotStack != null) {
            // Calculate the stack limit
            int stackLimit = Math.min(this.getInventoryStackLimit(), slotStack.getMaxStackSize());

            // Calculate how much can be added
            added = Math.min(amount, stackLimit - slotStack.stackSize);

            // Can any be added?
            if (added > 0) {
                this.slots[slotIndex].stackSize += added;
                this.markDirty();
            }
        }

        return added;
    }

    /**
     * True if the inventory is empty.
     *
     * @return
     */
    public boolean isEmpty() {
        // Loop over the slots
        for (int index = 0; index < this.slots.length; ++index) {
            // Is this slot occupied?
            if (this.slots[index] != null) {
                // Inventory is not empty
                return false;
            }
        }

        // Checked all slots, inventory is empty.
        return true;
    }

    /**
     * Must override, always return false.
     */
    @Override
    public boolean isItemValidForSlot(final int slotIndex, final ItemStack itemStack) {
        // Assume it is not valid
        return false;
    }

    /**
     * Always returns true.
     */
    @Override
    @Deprecated
    public boolean isUseableByPlayer(final EntityPlayer player) {
        return true;
    }

    public boolean isUseableByPlayer(@Nonnull final EntityPlayer player, @Nonnull final TileEntity tile) {
        return ThEUtils.canPlayerInteractWith(player, tile);
    }

    @Override
    public void markDirty() {}

    @Override
    public void openInventory() {}

    public final void readFromNBT(final NBTTagCompound data, final String tagName) {
        // Ensure there is a data tag
        if (data == null) {
            return;
        }

        // Ensure the data has our inventory
        if (!data.hasKey(tagName)) {
            return;
        }

        // Load the list
        NBTTagList invList = data.getTagList(tagName, (byte) 10);

        // Load the inventory from the list
        for (int index = 0; index < invList.tagCount(); index++) {
            // Get the compound
            NBTTagCompound nbtCompound = invList.getCompoundTagAt(index);

            // Get the slot number
            int slotIndex = nbtCompound.getByte(TheInternalInventory.NBT_KEY_SLOT) & 0xFF;

            // Validate the slot number
            if ((slotIndex >= 0) && (slotIndex < this.slots.length)) {
                // Load the itemstack
                this.slots[slotIndex] = ItemStack.loadItemStackFromNBT(nbtCompound);
            }
        }
    }

    @Override
    public void setInventorySlotContents(final int slotIndex, final ItemStack itemStack) {
        // Is the stack size too large?
        if ((itemStack != null) && (itemStack.stackSize > this.getInventoryStackLimit())) {
            itemStack.stackSize = this.getInventoryStackLimit();
        }

        this.slots[slotIndex] = itemStack;

        this.markDirty();
    }

    public final void writeToNBT(final NBTTagCompound data, final String tagName) {
        // Ensure there is a data tag
        if (data == null) {
            return;
        }

        // Create the list
        NBTTagList invList = new NBTTagList();

        // Write each slot into the list
        for (int slotIndex = 0; slotIndex < this.slots.length; slotIndex++) {
            // Ensure the slot has something to write
            if (this.slots[slotIndex] != null) {
                // Create the compound
                NBTTagCompound nbtCompound = new NBTTagCompound();

                // Set the slot number
                nbtCompound.setByte(TheInternalInventory.NBT_KEY_SLOT, (byte) slotIndex);

                // Save the inventory
                this.slots[slotIndex].writeToNBT(nbtCompound);

                // Add to the list
                invList.appendTag(nbtCompound);
            }
        }

        // Write the list into the data
        if (invList.tagCount() > 0) {
            data.setTag(tagName, invList);
        }
    }
}
