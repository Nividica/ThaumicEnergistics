package thaumicenergistics.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import thaumicenergistics.api.storage.IAEEssentiaStack;
import thaumicenergistics.client.gui.helpers.EssentiaRepo;

/**
 * @author BrockWS
 */
public class SlotMEEssentia extends Slot {

    private static IInventory EMPTY = new InventoryBasic("[Null]", true, 0);

    private EssentiaRepo repo;

    public SlotMEEssentia(EssentiaRepo repo, int index, int xPosition, int yPosition) {
        super(EMPTY, index, xPosition, yPosition);
        this.repo = repo;
    }

    public IAEEssentiaStack getAEStack() {
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
