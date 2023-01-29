package thaumicenergistics.common.container;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import thaumicenergistics.common.utils.ThEUtils;

/**
 * Container that includes the players inventory.
 *
 * @author Nividica
 *
 */
public abstract class ContainerWithPlayerInventory extends TheContainerBase {

    /**
     * The number of rows in the player inventory
     */
    private static int ROWS = 3;

    /**
     * The number of columns in the player inventory
     */
    private static int COLUMNS = 9;

    /**
     * The width and height of the slots
     */
    protected static final int SLOT_SIZE = 18;

    /**
     * X position offset for inventory slots
     */
    private static final int INVENTORY_X_OFFSET = 8;

    /**
     * Array of player hotbar slots
     */
    private final Slot[] hotbarSlots = new Slot[ContainerWithPlayerInventory.COLUMNS];

    /**
     * Array of player inventory slots.
     */
    private final Slot[] playerSlots = new Slot[ContainerWithPlayerInventory.COLUMNS
            * ContainerWithPlayerInventory.ROWS];

    public ContainerWithPlayerInventory(final EntityPlayer player) {
        super(player);
    }

    /**
     * Attempt to merge the specified slot stack with the hotbar inventory
     *
     * @param slotStack
     * @return
     */
    protected final boolean mergeSlotWithHotbarInventory(final ItemStack slotStack) {
        return this.mergeItemStack(
                slotStack,
                this.hotbarSlots[0].slotNumber,
                this.hotbarSlots[ContainerWithPlayerInventory.COLUMNS - 1].slotNumber + 1,
                false);
    }

    /**
     * Attempt to merge the specified slot stack with the player inventory
     *
     * @param slotStack
     * @return
     */
    protected final boolean mergeSlotWithPlayerInventory(final ItemStack slotStack) {
        return this.mergeItemStack(
                slotStack,
                this.playerSlots[0].slotNumber,
                this.playerSlots[(ContainerWithPlayerInventory.COLUMNS * ContainerWithPlayerInventory.ROWS)
                        - 1].slotNumber + 1,
                false);
    }

    /**
     * Checks if the slot clicked was in the hotbar inventory
     *
     * @param slotNumber
     * @return True if it was in the hotbar inventory, false otherwise.
     */
    protected final boolean slotClickedWasInHotbarInventory(final int slotNumber) {
        return (slotNumber >= this.hotbarSlots[0].slotNumber)
                && (slotNumber <= this.hotbarSlots[ContainerWithPlayerInventory.COLUMNS - 1].slotNumber);
    }

    /**
     * Checks if the slot clicked was in the player inventory
     *
     * @param slotNumber
     * @return True if it was in the player inventory, false otherwise.
     */
    protected final boolean slotClickedWasInPlayerInventory(final int slotNumber) {
        return (slotNumber >= this.playerSlots[0].slotNumber) && (slotNumber
                <= this.playerSlots[(ContainerWithPlayerInventory.COLUMNS * ContainerWithPlayerInventory.ROWS)
                        - 1].slotNumber);
    }

    /**
     * Attempt to move the item from hotbar <-> player inventory
     *
     * @param slotNumber
     * @return
     */
    protected final boolean swapSlotInventoryHotbar(final int slotNumber, final ItemStack slotStack) {
        if (this.slotClickedWasInHotbarInventory(slotNumber)) {
            return this.mergeSlotWithPlayerInventory(slotStack);
        } else if (this.slotClickedWasInPlayerInventory(slotNumber)) {
            return this.mergeSlotWithHotbarInventory(slotStack);
        }

        return false;
    }

    /**
     * Searches the players inventory & hotbar for the specified stack.
     *
     * @param searchStack
     * @param amount
     * @return
     */
    @Nullable
    protected final ItemStack takeItemFromPlayer(final ItemStack searchStack, final int amount) {
        // Sanity check
        if ((searchStack == null) || (amount <= 0)) {
            return null;
        }

        Slot matchingSlot = null;

        // Search inventory
        for (Slot slot : this.playerSlots) {
            // Empty?
            if (!slot.getHasStack()) {
                // Skip
                continue;
            }

            // Check for match
            if (ThEUtils.areStacksEqualIgnoreAmount(searchStack, slot.getStack())) {
                // Found match
                matchingSlot = slot;
                break;
            }
        }

        if (matchingSlot == null) {
            // Search hotbar
            for (Slot slot : this.hotbarSlots) {
                // Empty?
                if (!slot.getHasStack()) {
                    // Skip
                    continue;
                }

                // Check for match
                if (ThEUtils.areStacksEqualIgnoreAmount(searchStack, slot.getStack())) {
                    // Found match
                    matchingSlot = slot;
                    break;
                }
            }
        }

        if (matchingSlot == null) {
            // No matches
            return null;
        }

        // Check amount
        ItemStack matchStack = matchingSlot.getStack();
        if (matchStack.stackSize < amount) {
            // Not enough to take
            return null;
        }

        // Split
        ItemStack result = matchStack.splitStack(amount);

        // Anything left in the stack?
        if (matchStack.stackSize == 0) {
            // Null the slot
            matchingSlot.putStack(null);
        } else {
            // Update the slot
            matchingSlot.putStack(matchStack);
        }

        return result;
    }

    /**
     * Binds the player inventory to this container.
     *
     * @param playerInventory  Inventory to bind
     * @param inventoryOffsetY The Y position offset for the slots
     * @param hotbarPositionY  The Y position offset for hotbar slots
     */
    public final void bindPlayerInventory(final IInventory playerInventory, final int inventoryOffsetY,
            final int hotbarPositionY) {
        // Hot-bar ID's 0-8
        for (int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++) {
            // Create the slot
            this.hotbarSlots[column] = new Slot(
                    playerInventory,
                    column,
                    ContainerWithPlayerInventory.INVENTORY_X_OFFSET + (column * ContainerWithPlayerInventory.SLOT_SIZE),
                    hotbarPositionY);

            // Add the slot
            this.addSlotToContainer(this.hotbarSlots[column]);
        }

        // Main inventory ID's 9-36
        for (int row = 0; row < ContainerWithPlayerInventory.ROWS; row++) {
            for (int column = 0; column < ContainerWithPlayerInventory.COLUMNS; column++) {
                // Calculate index
                int index = column + (row * ContainerWithPlayerInventory.COLUMNS);

                // Create the slot
                this.playerSlots[index] = new Slot(
                        playerInventory,
                        ContainerWithPlayerInventory.COLUMNS + index,
                        ContainerWithPlayerInventory.INVENTORY_X_OFFSET
                                + (column * ContainerWithPlayerInventory.SLOT_SIZE),
                        (row * ContainerWithPlayerInventory.SLOT_SIZE) + inventoryOffsetY);

                // Add the slot
                this.addSlotToContainer(this.playerSlots[index]);
            }
        }
    }

    /**
     * Gets all non-empty slot from the hotbar inventory.
     *
     * @return
     */
    public final ArrayList<Slot> getNonEmptySlotsFromHotbar() {
        ArrayList<Slot> list = new ArrayList<Slot>();

        for (Slot slot : this.hotbarSlots) {
            if (slot.getHasStack()) {
                list.add(slot);
            }
        }

        return list;
    }

    /**
     * Gets all non-empty slot from the player inventory.
     *
     * @return
     */
    public final ArrayList<Slot> getNonEmptySlotsFromPlayerInventory() {
        ArrayList<Slot> list = new ArrayList<Slot>();

        for (Slot slot : this.playerSlots) {
            if (slot.getHasStack()) {
                list.add(slot);
            }
        }

        return list;
    }

    /**
     * Returns the first slot that contains the item and has room for more, or the first empty slot if no non-full
     * matches are found.
     *
     * @param stack
     * @return
     */
    public final Slot locateMergeSlot(final ItemStack stack) {
        Slot emptySlot = null;

        for (Slot slot : this.playerSlots) {
            if (slot.getHasStack()) {
                ItemStack slotStack = slot.getStack();
                // Same items, and room?
                if ((slotStack.stackSize < slotStack.getMaxStackSize()) && slotStack.isItemEqual(stack)
                        && ItemStack.areItemStackTagsEqual(slotStack, stack)) {
                    // Found merge slot
                    return slot;
                }
            } else if (emptySlot == null) {
                emptySlot = slot;
            }
        }
        return emptySlot;
    }
}
