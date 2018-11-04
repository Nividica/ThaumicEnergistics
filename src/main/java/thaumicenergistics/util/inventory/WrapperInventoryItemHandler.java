package thaumicenergistics.util.inventory;

import javax.annotation.Nonnull;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * @author BrockWS
 */
public class WrapperInventoryItemHandler implements IItemHandler {

    private IInventory inventory;

    public WrapperInventoryItemHandler(IInventory inventory) {
        this.inventory = inventory;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (slot < 0 || slot > this.getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
        if (!this.inventory.isItemValidForSlot(slot, stack))
            return stack;

        ItemStack existing = this.getStackInSlot(slot);

        int limit = Math.min(this.getSlotLimit(slot), stack.getMaxStackSize());

        if (!existing.isEmpty()) {
            if (!ItemHandlerHelper.canItemStacksStack(this.getStackInSlot(slot), stack))
                return stack;

            limit -= this.getStackInSlot(slot).getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (this.getStackInSlot(slot).isEmpty()) {
                this.inventory.setInventorySlotContents(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot < 0 || slot > this.getSlots())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + this.getSlots() + ")");
        if (amount <= 0)
            return ItemStack.EMPTY;

        ItemStack existing = this.getStackInSlot(slot);
        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= amount) {
            if (!simulate)
                this.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
            return existing;
        } else {
            if (!simulate)
                this.inventory.setInventorySlotContents(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStackInSlot(slot);
    }

    @Override
    public int getSlots() {
        return this.inventory.getSizeInventory();
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.inventory.getInventoryStackLimit();
    }
}
