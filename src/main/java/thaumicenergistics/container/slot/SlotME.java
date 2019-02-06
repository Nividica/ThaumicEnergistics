package thaumicenergistics.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.storage.data.IAEStack;

import thaumicenergistics.client.gui.helpers.MERepo;

/**
 * @author BrockWS
 */
public class SlotME<T extends IAEStack<T>> extends ThESlot {

    private static IInventory EMPTY = new InventoryBasic("[Null]", true, 0);

    private MERepo<T> repo;

    public SlotME(MERepo<T> repo, int index, int xPosition, int yPosition) {
        super(null, index, xPosition, yPosition, false);
        this.repo = repo;
    }

    public T getAEStack() {
        return this.repo.getReferenceStack(this.getSlotIndex());
    }

    @Override
    public ItemStack getStack() {
        if (this.getAEStack() == null || this.getAEStack().asItemStackRepresentation() == null)
            return ItemStack.EMPTY;
        return this.getAEStack().asItemStackRepresentation();
    }

    @Override
    public boolean getHasStack() {
        return !this.getStack().isEmpty();
    }

    @Override
    public void putStack(ItemStack stack) {

    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }
}
