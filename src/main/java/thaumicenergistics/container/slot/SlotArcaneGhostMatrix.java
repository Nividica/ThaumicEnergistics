package thaumicenergistics.container.slot;

import thaumicenergistics.container.ICraftingContainer;

/**
 * @author Alex811
 */
public class SlotArcaneGhostMatrix extends ThEGhostSlot{
    private ICraftingContainer container;

    public SlotArcaneGhostMatrix(ICraftingContainer container, int index, int xPosition, int yPosition) {
    super(container.getInventory("crafting"), index, xPosition, yPosition);
        this.container = container;
    }

    @Override
    public void onSlotChanged() {
        this.container.onMatrixChanged();
        super.onSlotChanged();
    }
}
